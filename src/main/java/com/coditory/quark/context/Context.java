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
import static com.coditory.quark.context.BeanFinalizer.closeBean;
import static com.coditory.quark.context.BeanInitializer.initializeBean;
import static com.coditory.quark.context.ResolutionPath.emptyResolutionPath;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

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

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Map<Class<?>, List<CacheableBeanCreator<?>>> beanCreatorsByType;
    private final Map<BeanDescriptor, List<CacheableBeanCreator<?>>> beanCreators;
    private final Set<String> beanNames;
    private final Set<Object> createdBeans = new HashSet<>();
    private final ResolutionContext emptyResolutionContext = new ResolutionContext(this, emptyResolutionPath());
    private boolean closed = false;

    Context(Map<BeanDescriptor, List<CacheableBeanCreator<?>>> beanCreators) {
        this.beanCreators = requireNonNull(beanCreators);
        this.beanCreatorsByType = groupBeanCreatorsByType(beanCreators);
        this.beanNames = beanCreators.keySet().stream()
                .map(BeanDescriptor::getName)
                .collect(toSet());
    }

    private Map<Class<?>, List<CacheableBeanCreator<?>>> groupBeanCreatorsByType(Map<BeanDescriptor, List<CacheableBeanCreator<?>>> beanCreators) {
        Map<Class<?>, List<CacheableBeanCreator<?>>> beanCreatorsByType = new HashMap<>();
        beanCreators.forEach((key, value) -> {
            List<CacheableBeanCreator<?>> creators = beanCreatorsByType.computeIfAbsent(key.getType(), (k) -> new ArrayList<>());
            creators.addAll(value);
        });
        return beanCreatorsByType.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, e -> unmodifiableList(e.getValue())));
    }

    void init() {
        beanCreators.values().stream()
                .flatMap(Collection::stream)
                .filter(CacheableBeanCreator::isCached)
                .forEach(creator -> {
                    Object bean = creator.create(emptyResolutionContext);
                    initializeBean(bean, emptyResolutionContext);
                    createdBeans.add(bean);
                });
    }

    public <T> T get(Class<T> type) {
        checkNonNull(type, "type");
        return get(type, emptyResolutionPath());
    }

    <T> T get(Class<T> type, ResolutionPath path) {
        T bean = getOrNull(type, path);
        if (bean == null) {
            throw new ContextException("Could not find bean of type: " + type.getCanonicalName());
        }
        return bean;
    }

    public <T> T getOrNull(Class<T> type) {
        checkNonNull(type, "type");
        return getOrNull(type, emptyResolutionPath());
    }

    <T> T getOrNull(Class<T> type, ResolutionPath path) {
        List<CacheableBeanCreator<?>> creators = beanCreators.get(new BeanDescriptor(type));
        if (creators == null || creators.isEmpty()) {
            return null;
        }
        if (creators.size() > 1) {
            throw new ContextException("Expected single bean of type: " + type + ". Got: " + creators.size());
        }
        BeanCreator<?> creator = creators.get(0);
        try {
            return createBean(creator, type, path.add(type));
        } catch (Exception e) {
            Throwable cause = simplifyException(e, path);
            throw new ContextException("Could not create bean of type: " + type.getCanonicalName(), cause);
        }
    }

    public <T> T get(Class<T> type, String name) {
        checkNonNull(type, "type");
        checkNonNull(name, "name");
        return get(type, name, emptyResolutionPath());
    }

    <T> T get(Class<T> type, String name, ResolutionPath path) {
        T bean = getOrNull(type, name, path);
        if (bean == null) {
            throw new ContextException("Could not find bean with name: " + name);
        }
        return bean;
    }

    public <T> T getOrNull(Class<T> type, String name) {
        checkNonNull(type, "type");
        checkNonNull(name, "name");
        return getOrNull(type, name, emptyResolutionPath());
    }

    <T> T getOrNull(Class<T> type, String name, ResolutionPath path) {
        List<CacheableBeanCreator<?>> creators = beanCreators.get(new BeanDescriptor(type, name));
        if (creators == null || creators.isEmpty()) {
            return null;
        }
        if (creators.size() > 1) {
            throw new ContextException("Expected single bean of type: " + type + ". Got: " + creators.size());
        }
        BeanCreator<?> creator = creators.get(0);
        try {
            return createBean(creator, type, path.add(type, name));
        } catch (Exception e) {
            Throwable cause = simplifyException(e, path);
            throw new ContextException("Could not create bean for name: " + name, cause);
        }
    }

    public boolean contains(Class<?> type) {
        checkNonNull(type, "type");
        return beanCreatorsByType.containsKey(type);
    }

    public boolean contains(String name) {
        checkNonNull(name, "name");
        return beanNames.contains(name);
    }

    public boolean contains(Class<?> type, String name) {
        checkNonNull(type, "type");
        checkNonNull(name, "name");
        return beanCreators.containsKey(new BeanDescriptor(type, name));
    }

    public <T> List<T> getAll(Class<T> type) {
        checkNonNull(type, "type");
        return getAll(type, emptyResolutionPath());
    }

    <T> List<T> getAll(Class<T> type, ResolutionPath path) {
        List<T> beans = getAllOrEmpty(type, path);
        if (beans.isEmpty()) {
            throw new ContextException("Beans not found for type: " + type.getCanonicalName());
        }
        return beans;
    }

    public <T> List<T> getAllOrEmpty(Class<T> type) {
        checkNonNull(type, "type");
        return getAllOrEmpty(type, emptyResolutionPath());
    }

    <T> List<T> getAllOrEmpty(Class<T> type, ResolutionPath path) {
        List<CacheableBeanCreator<?>> creators = beanCreatorsByType.get(type);
        if (creators == null || creators.isEmpty()) {
            return List.of();
        }
        try {
            return creators.stream()
                    .map(creator -> createBean(creator, type, path.add(type)))
                    .collect(toList());
        } catch (Exception e) {
            Throwable cause = simplifyException(e, path);
            throw new ContextException("Could not create beans for type: " + type.getCanonicalName(), cause);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T createBean(BeanCreator<?> creator, Class<T> type, ResolutionPath path) {
        if (closed) {
            throw new ContextException("Context already closed");
        }
        ResolutionContext resolutionContext = new ResolutionContext(this, path);
        Object bean = creator.create(resolutionContext);
        createdBeans.add(bean);
        initializeBean(bean, resolutionContext);
        return (T) bean;
    }

    @Override
    public void close() {
        HashSet<Object> closedBeans = new HashSet<>();
        while (closedBeans.size() < createdBeans.size()) {
            Set.copyOf(createdBeans).stream()
                    .filter(bean -> !closedBeans.contains(bean))
                    .forEach(bean -> {
                        closeBean(bean, emptyResolutionContext);
                        closedBeans.add(bean);
                    });
        }
        closed = true;
    }

    private Throwable simplifyException(Throwable e, ResolutionPath path) {
        if (!path.isEmpty()) {
            return e;
        }
        ContextException rootCause = Throwables.getRootCauseOfType(e, ContextException.class);
        return rootCause == null ? e : rootCause;
    }
}
