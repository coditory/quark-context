package com.coditory.quark.context;

import com.coditory.quark.context.events.ContextEvent;
import com.coditory.quark.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.coditory.quark.context.BeanDescriptor.descriptor;
import static com.coditory.quark.context.Preconditions.expectNonNull;
import static com.coditory.quark.context.ResolutionPath.emptyResolutionPath;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

public final class Context implements Closeable {
    @NotNull
    public static Context scanPackage(@NotNull Class<?> type) {
        expectNonNull(type, "type");
        return builder()
                .scanPackage(type)
                .build();
    }

    @NotNull
    public static ContextBuilder builder() {
        return new ContextBuilder();
    }

    static Context create(String name, Set<BeanHolder<?>> beanHolders, Set<BeanHolder<?>> initBeanHolders, Map<String, Object> properties, EventBus eventBus) {
        eventBus.emit(new ContextEvent.ContextPreCreateEvent());
        Timer totalTimer = Timer.start();
        Map<BeanDescriptor<?>, List<BeanHolder<?>>> holders = ContextResolver.resolve(beanHolders, properties);
        Context context = new Context(name, holders, properties, eventBus);
        context.init(initBeanHolders);
        log.info("Created context in {}", totalTimer.measureAndFormat());
        eventBus.emit(new ContextEvent.ContextPostCreateEvent());
        return context;
    }

    static Context createEager(String name, Set<BeanHolder<?>> beanHolders, Set<BeanHolder<?>> initBeanHolders, Map<String, Object> properties, EventBus eventBus) {
        eventBus.emit(new ContextEvent.ContextPreCreateEvent());
        Timer totalTimer = Timer.start();
        Map<BeanDescriptor<?>, List<BeanHolder<?>>> holders = ContextResolver.resolve(beanHolders, properties);
        Context context = new Context(name, holders, properties, eventBus);
        context.init(initBeanHolders);
        try {
            holders.forEach((descriptor, creator) -> {
                if (descriptor.name() != null) {
                    context.get(descriptor.type(), descriptor.name());
                } else if (descriptor.type() != Object.class) {
                    // Object is too generic, can be skipped for more readable exceptions
                    context.getAll(descriptor.type());
                }
            });
        } catch (Throwable exception) {
            try {
                context.close();
            } catch (Throwable closeException) {
                log.warn("Could not close context", closeException);
            }
            throw exception;
        }
        log.info("Created eager context in {}", totalTimer.measureAndFormat());
        eventBus.emit(new ContextEvent.ContextPostCreateEvent());
        return context;
    }

    private static final Logger log = LoggerFactory.getLogger(Context.class);
    private final String name;
    private final Map<Class<?>, List<BeanHolder<?>>> beanHoldersByType;
    private final Map<BeanDescriptor<?>, List<BeanHolder<?>>> beanHolders;
    private final Set<BeanHolder<?>> holders;
    private final Set<String> beanNames;
    private final Map<String, Object> properties;
    private final EventBus eventBus;
    private boolean closed = false;

    private Context(String name, Map<BeanDescriptor<?>, List<BeanHolder<?>>> beanHolders, Map<String, Object> properties, EventBus eventBus) {
        requireNonNull(name);
        requireNonNull(beanHolders);
        requireNonNull(properties);
        requireNonNull(eventBus);
        this.name = name;
        this.eventBus = eventBus;
        this.beanHolders = beanHolders;
        this.properties = Map.copyOf(properties);
        this.beanHoldersByType = groupBeanCreatorsByType(beanHolders);
        this.holders = beanHolders.values().stream()
                .flatMap(Collection::stream)
                .collect(toCollection(LinkedHashSet::new));
        this.beanNames = beanHolders.keySet().stream()
                .map(BeanDescriptor::name)
                .collect(toCollection(LinkedHashSet::new));
    }

    @NotNull
    public String getName() {
        return name;
    }

    private void init(Set<BeanHolder<?>> initBeanHolders) {
        ResolutionContext context = new ResolutionContext(this, emptyResolutionPath());
        initBeanHolders.stream()
                .filter(BeanHolder::isEager)
                .forEach(holder -> holder.get(context));
        this.holders.stream()
                .filter(BeanHolder::isEager)
                .forEach(holder -> holder.get(context));
    }

    private Map<Class<?>, List<BeanHolder<?>>> groupBeanCreatorsByType(Map<BeanDescriptor<?>, List<BeanHolder<?>>> beanCreators) {
        Map<Class<?>, List<BeanHolder<?>>> beanCreatorsByType = new HashMap<>();
        beanCreators.forEach((key, value) -> {
            List<BeanHolder<?>> creators = beanCreatorsByType.computeIfAbsent(key.type(), (k) -> new ArrayList<>());
            creators.addAll(value);
        });
        return beanCreatorsByType.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, e -> unmodifiableList(e.getValue())));
    }

    @NotNull
    public <T> T get(@NotNull Class<T> type) {
        expectNonNull(type, "type");
        return get(descriptor(type), emptyResolutionPath());
    }

    @Nullable
    public <T> T getOrNull(@NotNull Class<T> type) {
        expectNonNull(type, "type");
        return getOrNull(descriptor(type), emptyResolutionPath());
    }

    @NotNull
    public <T> T get(@NotNull Class<T> type, @NotNull String name) {
        expectNonNull(type, "type");
        expectNonNull(name, "name");
        return get(descriptor(type, name), emptyResolutionPath());
    }

    @NotNull
    public <T> T get(@NotNull BeanDescriptor<T> descriptor) {
        expectNonNull(descriptor, "descriptor");
        return get(descriptor, emptyResolutionPath());
    }

    @Nullable
    public <T> T getOrNull(@NotNull Class<T> type, @NotNull String name) {
        expectNonNull(type, "type");
        expectNonNull(name, "name");
        return getOrNull(descriptor(type, name), emptyResolutionPath());
    }

    @Nullable
    public <T> T getOrNull(@NotNull BeanDescriptor<T> descriptor) {
        expectNonNull(descriptor, "descriptor");
        return getOrNull(descriptor, emptyResolutionPath());
    }

    <T> T get(BeanDescriptor<T> descriptor, ResolutionPath path) {
        T bean = getOrNull(descriptor, path);
        if (bean == null) {
            throw new ContextException("Could not find bean: " + descriptor.toShortString());
        }
        return bean;
    }

    <T> T getOrNull(BeanDescriptor<T> descriptor, ResolutionPath path) {
        List<BeanHolder<?>> holders = descriptor.hasName()
                ? beanHolders.get(descriptor)
                : beanHoldersByType.get(descriptor.type());
        if (holders == null || holders.isEmpty()) {
            return null;
        }
        List<BeanHolder<?>> namedHolders = holders.stream()
                .filter(holder -> holder.getDescriptor().hasName())
                .toList();
        List<BeanHolder<?>> unnamedHolders = holders.stream()
                .filter(holder -> !holder.getDescriptor().hasName())
                .toList();
        if (holders.size() > 1 && unnamedHolders.size() != 1) {
            throw new ContextException("Expected single bean: " + descriptor.toShortString()
                    + ". Found " + holders.size() + " beans.");
        }
        BeanHolder<?> holder = unnamedHolders.size() == 1
                ? unnamedHolders.get(0)
                : namedHolders.get(0);
        try {
            return createBean(holder, descriptor, path);
        } catch (Exception e) {
            Throwable cause = simplifyException(e, path);
            throw new ContextException("Could not create bean: " + descriptor.toShortString(), cause);
        }
    }

    public boolean contains(@NotNull Class<?> type) {
        expectNonNull(type, "type");
        return beanHoldersByType.containsKey(type);
    }

    public boolean contains(@NotNull String name) {
        expectNonNull(name, "name");
        return beanNames.contains(name);
    }

    public boolean contains(@NotNull Class<?> type, @NotNull String name) {
        expectNonNull(type, "type");
        expectNonNull(name, "name");
        return beanHolders.containsKey(descriptor(type, name));
    }

    @NotNull
    public <T> List<T> getAll(@NotNull Class<T> type) {
        expectNonNull(type, "type");
        return getAll(type, emptyResolutionPath());
    }

    <T> List<T> getAll(Class<T> type, ResolutionPath path) {
        List<T> beans = getAllOrEmpty(type, path);
        if (beans.isEmpty()) {
            throw new ContextException("Beans not found for type: " + type.getSimpleName());
        }
        return beans;
    }

    @NotNull
    public <T> List<T> getAllOrEmpty(@NotNull Class<T> type) {
        expectNonNull(type, "type");
        return getAllOrEmpty(type, emptyResolutionPath());
    }

    <T> List<T> getAllOrEmpty(Class<T> type, ResolutionPath path) {
        BeanDescriptor<T> descriptor = descriptor(type);
        List<BeanHolder<?>> creators = beanHoldersByType.get(type);
        if (creators == null || creators.isEmpty()) {
            return List.of();
        }
        try {
            return creators.stream()
                    .map(creator -> createBean(creator, descriptor, path))
                    .collect(toList());
        } catch (Exception e) {
            Throwable cause = simplifyException(e, path);
            throw new ContextException("Could not create beans: " + descriptor.toShortString(), cause);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T createBean(BeanHolder<?> holder, BeanDescriptor<T> descriptor, ResolutionPath path) {
        if (closed) {
            throw new ContextException("Context already closed");
        }
        ResolutionContext resolutionContext = new ResolutionContext(this, path);
        Object bean = holder.get(resolutionContext);
        return (T) bean;
    }

    @Override
    public void close() {
        eventBus.emit(new ContextEvent.ContextPreCloseEvent());
        ResolutionContext emptyResolutionContext = new ResolutionContext(this, emptyResolutionPath());
        Set<BeanHolder<?>> closedBeanHolders;
        long createdBeans;
        do {
            closedBeanHolders = holders.stream()
                    .filter(BeanHolder::isCached)
                    .collect(toCollection(LinkedHashSet::new));
            closedBeanHolders.forEach(b -> b.close(emptyResolutionContext));
            createdBeans = holders.stream()
                    .filter(BeanHolder::isCached)
                    .count();
        } while (closedBeanHolders.size() < createdBeans);
        closed = true;
        eventBus.emit(new ContextEvent.ContextPostCloseEvent());
    }

    private Throwable simplifyException(Throwable e, ResolutionPath path) {
        if (!path.isEmpty()) {
            return e;
        }
        CyclicDependencyException rootCause = Throwables.getRootCauseOfType(e, CyclicDependencyException.class);
        return rootCause == null ? e : rootCause;
    }

    @Override
    public String toString() {
        int hashCode = this.hashCode();
        return "Context{name='" + name + "', hashCode='" + hashCode + "', beanNames=" + beanNames + "}";
    }
}
