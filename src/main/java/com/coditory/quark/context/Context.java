package com.coditory.quark.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.coditory.quark.context.CacheableBeanCreator.cacheable;
import static java.util.stream.Collectors.toList;

public class Context implements Closeable {
    public static ContextBuilder builder() {
        return new ContextBuilder();
    }

    static Context create(
            Map<Class<?>, List<BeanCreator<?>>> creatorsByType,
            Map<String, BeanCreator<?>> beanCreatorsByName
    ) {
        return new Context(
                wrapCreatorsByType(creatorsByType),
                wrapCreatorsByName(beanCreatorsByName),
                List.of()
        );
    }

    private static Map<Class<?>, List<BeanCreator<?>>> wrapCreatorsByType(Map<Class<?>, List<BeanCreator<?>>> beanCreatorsByType) {
        Map<Class<?>, List<BeanCreator<?>>> copy = new LinkedHashMap<>();
        beanCreatorsByType.forEach((type, creators) -> copy.put(type, cacheable(creators)));
        return Map.copyOf(copy);
    }

    private static Map<String, BeanCreator<?>> wrapCreatorsByName(Map<String, BeanCreator<?>> beanCreatorsByName) {
        Map<String, BeanCreator<?>> copy = new LinkedHashMap<>();
        beanCreatorsByName.forEach((name, creator) -> copy.put(name, cacheable(creator)));
        return Map.copyOf(copy);
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Map<Class<?>, List<BeanCreator<?>>> beanCreatorsByType;
    private final Map<String, BeanCreator<?>> beanCreatorsByName;
    private final Set<Object> beans = new HashSet<>();

    private Context(
            Map<Class<?>, List<BeanCreator<?>>> beanCreatorsByType,
            Map<String, BeanCreator<?>> beanCreatorsByName,
            List<ResolutionElement> resolutionPath
    ) {
        this.beanCreatorsByType = beanCreatorsByType;
        this.beanCreatorsByName = beanCreatorsByName;
    }

    public <T> T get(Class<T> clazz) {
        return get(clazz, List.of());
    }

    <T> T get(Class<T> clazz, List<ResolutionElement> path) {
        T bean = getOrNull(clazz, path);
        if (bean == null) {
            throw new ContextException("Could not find bean of type: " + clazz.getCanonicalName());
        }
        return bean;
    }

    public <T> T getOrNull(Class<T> clazz) {
        return getOrNull(clazz, List.of());
    }

    <T> T getOrNull(Class<T> clazz, List<ResolutionElement> path) {
        List<BeanCreator<?>> creators = beanCreatorsByType.get(clazz);
        if (creators == null || creators.isEmpty()) {
            return null;
        }
        if (creators.size() > 1) {
            throw new ContextException("Expected single bean for: " + clazz.getCanonicalName()
                    + " Got: " + creators.size());
        }
        BeanCreator<?> creator = creators.get(0);
        try {
            return createBean(creator, clazz, path);
        } catch (Exception e) {
            throw new ContextException("Could not create bean for type: " + clazz.getCanonicalName(), e);
        }
    }

    public <T> T get(String name, Class<T> clazz) {
        return get(name, clazz, List.of());
    }

    <T> T get(String name, Class<T> clazz, List<ResolutionElement> path) {
        T bean = getOrNull(name, clazz, path);
        if (bean == null) {
            throw new ContextException("Could not find bean of name: " + name);
        }
        return bean;
    }

    public <T> T getOrNull(String name, Class<T> clazz) {
        return getOrNull(name, clazz, List.of());
    }

    <T> T getOrNull(String name, Class<T> clazz, List<ResolutionElement> path) {
        BeanCreator<?> creator = beanCreatorsByName.get(name);
        if (creator == null) {
            throw new ContextException("Bean not found for name: " + name);
        }
        try {
            return createBean(creator, clazz, path);
        } catch (Exception e) {
            throw new ContextException("Could not create bean for name: " + name, e);
        }
    }

    public boolean contains(Class<?> clazz) {
        return beanCreatorsByType.containsKey(clazz);
    }

    public boolean contains(String name) {
        return beanCreatorsByName.containsKey(name);
    }

    public <T> List<T> getAll(Class<T> clazz) {
        return getAll(clazz, List.of());
    }

    <T> List<T> getAll(Class<T> clazz, List<ResolutionElement> path) {
        List<T> beans = getAllOrEmpty(clazz, path);
        if (beans.isEmpty()) {
            throw new ContextException("Beans not found for type: " + clazz.getCanonicalName());
        }
        return beans;
    }

    public <T> List<T> getAllOrEmpty(Class<T> clazz) {
        return getAllOrEmpty(clazz, List.of());
    }

    <T> List<T> getAllOrEmpty(Class<T> clazz, List<ResolutionElement> path) {
        List<BeanCreator<?>> creators = beanCreatorsByType.get(clazz);
        if (creators == null || creators.isEmpty()) {
            return List.of();
        }
        try {
            return creators.stream()
                    .map(creator -> createBean(creator, clazz, path))
                    .collect(toList());
        } catch (Exception e) {
            throw new ContextException("Could not create beans for type: " + clazz.getCanonicalName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T createBean(BeanCreator<?> creator, Class<T> clazz, List<ResolutionElement> path) {
        ResolutionContext resolutionContext = new ResolutionContext(this, path);
        Object bean = creator.create(resolutionContext);
        beans.add(bean);
        return (T) bean;
    }

    @Override
    public void close() {
        beans.stream()
                .filter(bean -> bean instanceof Closeable)
                .map(bean -> (Closeable) bean)
                .forEach(bean -> {
                    try {
                        bean.close();
                    } catch (IOException e) {
                        logger.warn("Could not close bean: " + bean, e);
                    }
                });
    }
}
