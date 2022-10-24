package com.coditory.quark.context;

import com.coditory.quark.context.events.ContextEvent;
import com.coditory.quark.eventbus.EventEmitter;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static com.coditory.quark.context.BeanFinalizer.closeBean;
import static com.coditory.quark.context.BeanInitializer.initializeBean;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

final class BeanHolder<T> {
    static <T> BeanHolder<T> holder(BeanDescriptor<T> descriptor, BeanCreator<T> creator, boolean eager) {
        return new BeanHolder<>(descriptor, creator, eager);
    }

    static <T> BeanHolder<T> holder(BeanDescriptor<T> descriptor, BeanCreator<T> creator) {
        return new BeanHolder<>(descriptor, creator, false);
    }

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final BeanCreator<T> creator;
    private final BeanDescriptor<T> descriptor;
    private final Set<Class<?>> classHierarchy;
    private final boolean eager;
    private EventEmitter eventEmitter;
    private T bean;
    private boolean initialized = false;
    private boolean closed = false;

    private BeanHolder(BeanDescriptor<T> descriptor, BeanCreator<T> creator, boolean eager) {
        this.creator = requireNonNull(creator);
        this.descriptor = requireNonNull(descriptor);
        this.classHierarchy = HierarchyIterator.getClassHierarchy(descriptor.type());
        this.eager = eager;
    }

    void setEventEmitter(EventEmitter eventEmitter) {
        if (this.eventEmitter != null) {
            throw new IllegalStateException("EventEmitter was already set");
        }
        this.eventEmitter = requireNonNull(eventEmitter);
    }

    public boolean isEager() {
        return eager;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isClosed() {
        return closed;
    }

    Class<T> getBeanType() {
        return descriptor.type();
    }

    String getBeanName() {
        return descriptor.name();
    }

    Set<Class<?>> getBeanClassHierarchy() {
        return classHierarchy;
    }

    Set<BeanDescriptor<?>> getBeanClassHierarchyDescriptors() {
        return classHierarchy.stream()
                .map(descriptor::withType)
                .collect(toSet());
    }

    public BeanDescriptor<T> getDescriptor() {
        return descriptor;
    }

    boolean isCached() {
        return bean != null;
    }

    T getCached() {
        return bean;
    }

    boolean isActive(ConditionContext context) {
        expectEventEmitter();
        eventEmitter.emit(new ContextEvent.BeanPreIsActiveCheckEvent(descriptor));
        boolean result = creator.isActive(context);
        eventEmitter.emit(new ContextEvent.BeanPostIsActiveCheckEvent(descriptor, result));
        return result;
    }

    @Nullable
    public T get(ResolutionContext context) {
        if (bean == null) {
            expectEventEmitter();
            ResolutionPath path = context.getResolutionPath();
            eventEmitter.emit(new ContextEvent.BeanPreCreateEvent(descriptor, path));
            createBean(context);
            eventEmitter.emit(new ContextEvent.BeanPostCreateEvent(descriptor, path));
        }
        return bean;
    }

    private void createBean(ResolutionContext context) {
        Timer timer = Timer.start();
        bean = creator.create(context);
        if (bean == null) {
            throw new ContextException("Expected non-null bean: " + descriptor);
        }
        log.debug("Created bean {} in {}", descriptor.toShortString(), timer.measureAndFormat());
        if (!initialized) {
            initializeBean(bean, descriptor, context);
            initialized = true;
        }
    }

    void close(ResolutionContext context) {
        if (!closed) {
            expectEventEmitter();
            eventEmitter.emit(new ContextEvent.BeanPreCloseEvent(descriptor));
            closeBean(bean, descriptor, context);
            closed = true;
            eventEmitter.emit(new ContextEvent.BeanPostCloseEvent(descriptor));
        }
    }

    private void expectEventEmitter() {
        if (eventEmitter == null) {
            throw new IllegalStateException("Expected BeanHolder to have eventEmitter");
        }
    }
}
