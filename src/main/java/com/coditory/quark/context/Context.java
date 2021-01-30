package com.coditory.quark.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.coditory.quark.context.Args.checkNonNull;
import static com.coditory.quark.context.BeanDescriptor.descriptor;
import static com.coditory.quark.context.BeanFinalizer.closeBean;
import static com.coditory.quark.context.BeanInitializer.initializeBean;
import static com.coditory.quark.context.ResolutionPath.emptyResolutionPath;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toUnmodifiableSet;

public final class Context implements Closeable {
    public static Context scanPackage(Class<?> type) {
        checkNonNull(type, "type");
        return builder()
                .scanPackage(type)
                .build();
    }

    public static ContextBuilder builder() {
        return new ContextBuilder();
    }

    static Context create(Map<BeanDescriptor<?>, List<BeanHolder<?>>> holders, Map<String, Object> properties) {
        Context context = new Context(holders, properties);
        context.init();
        return context;
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Map<Class<?>, List<BeanHolder<?>>> beanHoldersByType;
    private final Map<BeanDescriptor<?>, List<BeanHolder<?>>> beanHolders;
    private final Set<BeanHolder<?>> holders;
    private final Set<String> beanNames;
    private final Map<String, Object> properties;
    private boolean closed = false;

    private Context(Map<BeanDescriptor<?>, List<BeanHolder<?>>> beanHolders, Map<String, Object> properties) {
        this.beanHolders = requireNonNull(beanHolders);
        this.properties = Map.copyOf(properties);
        this.beanHoldersByType = groupBeanCreatorsByType(beanHolders);
        this.holders = beanHolders.values().stream()
                .flatMap(Collection::stream)
                .collect(toUnmodifiableSet());
        this.beanNames = beanHolders.keySet().stream()
                .map(BeanDescriptor::getName)
                .collect(toSet());
    }

    private void init() {
        ResolutionContext context = new ResolutionContext(this, emptyResolutionPath());
        this.holders.stream()
                .filter(BeanHolder::isEager)
                .forEach(holder -> holder.get(context));
    }

    private Map<Class<?>, List<BeanHolder<?>>> groupBeanCreatorsByType(Map<BeanDescriptor<?>, List<BeanHolder<?>>> beanCreators) {
        Map<Class<?>, List<BeanHolder<?>>> beanCreatorsByType = new HashMap<>();
        beanCreators.forEach((key, value) -> {
            List<BeanHolder<?>> creators = beanCreatorsByType.computeIfAbsent(key.getType(), (k) -> new ArrayList<>());
            creators.addAll(value);
        });
        return beanCreatorsByType.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, e -> unmodifiableList(e.getValue())));
    }

    public <T> T get(Class<T> type) {
        checkNonNull(type, "type");
        return get(descriptor(type), emptyResolutionPath());
    }

    public <T> T getOrNull(Class<T> type) {
        checkNonNull(type, "type");
        return getOrNull(descriptor(type), emptyResolutionPath());
    }

    public <T> T get(Class<T> type, String name) {
        checkNonNull(type, "type");
        checkNonNull(name, "name");
        return get(descriptor(type, name), emptyResolutionPath());
    }

    public <T> T getOrNull(Class<T> type, String name) {
        checkNonNull(type, "type");
        checkNonNull(name, "name");
        return getOrNull(descriptor(type, name), emptyResolutionPath());
    }

    <T> T get(BeanDescriptor<T> descriptor, ResolutionPath path) {
        T bean = getOrNull(descriptor, path);
        if (bean == null) {
            throw new ContextException("Could not find bean: " + descriptor.toShortString());
        }
        return bean;
    }

    <T> T getOrNull(BeanDescriptor<T> descriptor, ResolutionPath path) {
        List<BeanHolder<?>> holders = beanHolders.get(descriptor);
        if (holders == null || holders.isEmpty()) {
            return null;
        }
        if (holders.size() > 1) {
            throw new ContextException("Expected single bean: " + descriptor.toShortString()
                    + ". Got: " + holders.size());
        }
        BeanHolder<?> holder = holders.get(0);
        try {
            return createBean(holder, descriptor, path.add(descriptor));
        } catch (Exception e) {
            Throwable cause = simplifyException(e, path);
            throw new ContextException("Could not create bean: " + descriptor.toShortString(), cause);
        }
    }

    public boolean contains(Class<?> type) {
        checkNonNull(type, "type");
        return beanHoldersByType.containsKey(type);
    }

    public boolean contains(String name) {
        checkNonNull(name, "name");
        return beanNames.contains(name);
    }

    public boolean contains(Class<?> type, String name) {
        checkNonNull(type, "type");
        checkNonNull(name, "name");
        return beanHolders.containsKey(descriptor(type, name));
    }

    public <T> List<T> getAll(Class<T> type) {
        checkNonNull(type, "type");
        return getAll(type, emptyResolutionPath());
    }

    <T> List<T> getAll(Class<T> type, ResolutionPath path) {
        List<T> beans = getAllOrEmpty(type, path);
        if (beans.isEmpty()) {
            throw new ContextException("Beans not found for type: " + type.getSimpleName());
        }
        return beans;
    }

    public <T> List<T> getAllOrEmpty(Class<T> type) {
        checkNonNull(type, "type");
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
                    .map(creator -> createBean(creator, descriptor, path.add(type)))
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
        ResolutionContext emptyResolutionContext = new ResolutionContext(this, emptyResolutionPath());
        Set<BeanHolder<?>> closedBeanHolders;
        long createdBeans;
        do {
            closedBeanHolders = holders.stream()
                    .filter(BeanHolder::isCached)
                    .collect(toSet());
            closedBeanHolders.forEach(b -> b.close(emptyResolutionContext));
            createdBeans = holders.stream()
                    .filter(BeanHolder::isCached)
                    .count();
        } while (closedBeanHolders.size() < createdBeans);
        closed = true;
    }

    private Throwable simplifyException(Throwable e, ResolutionPath path) {
        if (!path.isEmpty()) {
            return e;
        }
        CyclicDependencyException rootCause = Throwables.getRootCauseOfType(e, CyclicDependencyException.class);
        return rootCause == null ? e : rootCause;
    }
}
