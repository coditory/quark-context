package com.coditory.quark.context;

import com.coditory.quark.context.events.ContextEvent;
import com.coditory.quark.eventbus.EventEmitter;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static com.coditory.quark.context.BeanFinalizer.closeBean;
import static com.coditory.quark.context.BeanInitializer.initializeBean;
import static com.coditory.quark.context.BeanPostInitializer.postInitializeBean;
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
    private boolean postInitialized = false;
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

    boolean isPostInitialized() {
        return postInitialized;
    }

    void postInitialize(Context context) {
        if (postInitialized) return;
        if (bean == null) {
            throw new IllegalStateException("Expected bean to exist before post initialization");
        }
        ResolutionContext resolutionContext = new ResolutionContext(context, ResolutionPath.of(descriptor));
        postInitialized = true;
        postInitializeBean(bean, descriptor, resolutionContext);
    }

    Class<T> getBeanType() {
        return descriptor.type();
    }

    String getBeanName() {
        return descriptor.name();
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

    boolean isActive(ConditionContext context) {
        return creator.isActive(context);
    }

    @Nullable
    public T get(ResolutionContext context) {
        if (bean == null) {
            expectEventEmitter();
            ResolutionPath path = context.getResolutionPath();
            if (path.contains(descriptor)) {
                throw new CyclicDependencyException("Detected cyclic dependency: " + path.toPathAsString(descriptor));
            }
            path = path.add(descriptor);
            context = context.withPath(path);
            eventEmitter.emit(new ContextEvent.BeanPreCreateEvent(descriptor, path));
            createBean(context);
            eventEmitter.emit(new ContextEvent.BeanPostCreateEvent(descriptor, path));
        }
        return bean;
    }

    private void createBean(ResolutionContext context) {
        Timer timer = Timer.start();
        bean = creator.create(context);
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

    @Override
    public String toString() {
        return "BeanHolder{" + descriptor.toShortString() + '}';
    }
}

