package com.coditory.quark.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

public class ContextBuilder {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Map<Class<?>, List<BeanCreator<?>>> creatorsByType = new LinkedHashMap<>();
    private final Map<String, BeanCreator<?>> creatorsByName = new LinkedHashMap<>();

    public Context build() {
        return Context.create(creatorsByType, creatorsByName);
    }

    public Context buildEager() {
        Context context = Context.create(creatorsByType, creatorsByName);
        creatorsByName.forEach((name, __) -> context.get(name, Object.class));
        creatorsByType.forEach((type, __) -> context.getAll(type));
        return context;
    }

    public ContextBuilder scanPackageAndSubPackages(Class<?> type) {
        return scanPackageAndSubPackages(type.getPackageName(), name -> true);
    }

    public ContextBuilder scanPackageAndSubPackages(String packageName) {
        return scanPackageAndSubPackages(packageName, name -> true);
    }

    public ContextBuilder scanPackageAndSubPackages(String packageName, Predicate<String> canonicalNameFilter) {
        Iterable<Class<?>> iterable = () -> ClasspathScanner.scanPackageAndSubPackages(packageName, canonicalNameFilter);
        StreamSupport.stream(iterable.spliterator(), false)
                .forEach(this::scanClass);
        return this;
    }

    public ContextBuilder scanClass(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Module.class)) {
            if (clazz.isAnnotationPresent(Bean.class)) {
                throw new ContextException("Detected incompatible annotations on class: " + clazz.getCanonicalName());
            }
            addBeansFromAnnotatedContextModule(clazz);
        } else if (clazz.isAnnotationPresent(Bean.class)) {
            addAnnotatedBeanClass(clazz);
        }
        return this;
    }

    private <T> void addAnnotatedBeanClass(Class<T> clazz) {
        Bean annotation = clazz.getAnnotation(Bean.class);
        BeanCreator<T> creator = new CacheableBeanCreator<>(ConstructorBasedBeanCreator.fromConstructor(clazz));
        addAnnotatedCreator(annotation, clazz, creator);
    }

    private void addBeansFromAnnotatedContextModule(Class<?> clazz) {
        BeanCreator<?> objectCreator = new CacheableBeanCreator<>(ConstructorBasedBeanCreator.fromConstructor(clazz));
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Bean.class)) {
                method.setAccessible(true);
                addFromAnnotatedContextModule(objectCreator, method, method.getReturnType());
            }
        }
    }

    private <T> void addFromAnnotatedContextModule(BeanCreator<?> objectCreator, Method method, Class<T> returnType) {
        BeanCreator<T> creator = new MethodBasedBeanCreator<>(objectCreator, method);
        Bean annotation = method.getAnnotation(Bean.class);
        addAnnotatedCreator(annotation, returnType, creator);
    }

    private <T> void addAnnotatedCreator(Bean annotation, Class<T> clazz, BeanCreator<T> creator) {
        if (annotation.name().isBlank()) {
            add(clazz, creator);
        } else {
            add(annotation.name(), clazz, creator);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> ContextBuilder add(T bean) {
        return add((Class<T>) bean.getClass(), (ctx) -> bean);
    }

    @SuppressWarnings("unchecked")
    public <T> ContextBuilder add(String name, T bean) {
        return add(name, (Class<T>) bean.getClass(), (ctx) -> bean);
    }

    public <T> ContextBuilder add(Class<T> clazz, BeanCreator<T> beanCreator) {
        addByType(clazz, beanCreator);
        return this;
    }

    public <T> ContextBuilder add(String name, Class<T> clazz, BeanCreator<T> beanCreator) {
        addByType(clazz, beanCreator);
        addByName(name, beanCreator);
        return this;
    }

    private <T> void addByType(Class<T> clazz, BeanCreator<T> beanCreator) {
        Set<Class<?>> types = HierarchyIterator.getClassHierarchy(clazz);
        types.forEach(type -> {
            List<BeanCreator<?>> registeredBeanCreators = creatorsByType.getOrDefault(type, new ArrayList<>());
            registeredBeanCreators.add(beanCreator);
            creatorsByType.put(type, registeredBeanCreators);
        });
    }

    private void addByName(String name, BeanCreator<?> beanCreator) {
        if (creatorsByName.containsKey(name)) {
            throw new IllegalArgumentException("Duplicated bean name: " + name);
        }
        creatorsByName.put(name, beanCreator);
    }
}
