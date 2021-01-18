package com.coditory.quark.context;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import static com.coditory.quark.context.Args.checkNonNull;
import static com.coditory.quark.context.CacheableBeanCreator.cacheable;

public final class ContextBuilder {
    private final Map<BeanDescriptor, List<CacheableBeanCreator<?>>> beanCreators = new LinkedHashMap<>();
    private final Set<String> beanNames = new HashSet<>();

    public ContextBuilder scanPackage(Class<?> type) {
        checkNonNull(type, "type");
        return scanPackage(type.getPackageName(), name -> true);
    }

    public ContextBuilder scanPackage(Class<?> type, Predicate<String> canonicalNameFilter) {
        checkNonNull(type, "type");
        checkNonNull(canonicalNameFilter, "canonicalNameFilter");
        return scanPackage(type.getPackageName(), canonicalNameFilter);
    }

    public ContextBuilder scanPackage(String packageName) {
        checkNonNull(packageName, "packageName");
        return scanPackage(packageName, name -> true);
    }

    public ContextBuilder scanPackage(String packageName, Predicate<String> canonicalNameFilter) {
        checkNonNull(packageName, "packageName");
        checkNonNull(canonicalNameFilter, "canonicalNameFilter");
        Iterable<Class<?>> iterable = () -> ClasspathScanner.scanPackageAndSubPackages(packageName, canonicalNameFilter);
        StreamSupport.stream(iterable.spliterator(), false)
                .forEach(this::scanClass);
        return this;
    }

    public <T> ContextBuilder scanClass(Class<T> type) {
        checkNonNull(type, "type");
        if (type.isAnnotationPresent(Configuration.class)) {
            if (type.isAnnotationPresent(Bean.class)) {
                throw new ContextException("Detected incompatible annotations on class: " + type.getCanonicalName()
                        + ". Class should have one annotation: @Configuration or @Bean.");
            }
            addBeansFromConfiguration(type);
        } else if (type.isAnnotationPresent(Bean.class)) {
            addAnnotatedBeanClass(type);
        }
        return this;
    }

    private <T> void addAnnotatedBeanClass(Class<T> type) {
        Bean annotation = type.getAnnotation(Bean.class);
        BeanCreator<T> creator = new CacheableBeanCreator<>(ConstructorBasedBeanCreator.fromConstructor(type));
        addAnnotatedCreator(annotation, type, creator);
    }

    private <T> void addBeansFromConfiguration(Class<T> type) {
        BeanCreator<T> objectCreator = new CacheableBeanCreator<>(ConstructorBasedBeanCreator.fromConstructor(type));
        add(type, objectCreator);
        for (Method method : type.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Bean.class)) {
                method.setAccessible(true);
                addFromAnnotatedConfiguration(objectCreator, method, method.getReturnType());
            }
        }
    }

    private <T> void addFromAnnotatedConfiguration(BeanCreator<?> objectCreator, Method method, Class<T> returnType) {
        BeanCreator<T> creator = new MethodBasedBeanCreator<>(objectCreator, method);
        Bean annotation = method.getAnnotation(Bean.class);
        addAnnotatedCreator(annotation, returnType, creator);
    }

    private <T> void addAnnotatedCreator(Bean annotation, Class<T> type, BeanCreator<T> creator) {
        String name = annotation.name().isBlank()
                ? annotation.value()
                : annotation.name();
        if (name.isBlank()) {
            add(type, creator);
        } else {
            add(type, name, creator);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> ContextBuilder add(T bean) {
        checkNonNull(bean, "bean");
        return add((Class<T>) bean.getClass(), (ctx) -> bean);
    }

    @SuppressWarnings("unchecked")
    public <T> ContextBuilder add(T bean, String name) {
        checkNonNull(bean, "bean");
        checkNonNull(name, "name");
        return add((Class<T>) bean.getClass(), name, (ctx) -> bean);
    }

    public <T> ContextBuilder add(Class<T> type, BeanCreator<T> beanCreator) {
        checkNonNull(type, "type");
        checkNonNull(beanCreator, "beanCreator");
        addBeanCreator(new BeanDescriptor(type), beanCreator);
        return this;
    }

    public <T> ContextBuilder add(Class<T> type, String name, BeanCreator<T> beanCreator) {
        checkNonNull(type, "type");
        checkNonNull(name, "name");
        checkNonNull(beanCreator, "beanCreator");
        addBeanName(name);
        addBeanCreator(new BeanDescriptor(type, name), beanCreator);
        return this;
    }

    private <T> void addBeanName(String name) {
        if (beanNames.contains(name)) {
            throw new IllegalArgumentException("Duplicated bean name: " + name);
        }
        beanNames.add(name);
    }

    private <T> void addBeanCreator(BeanDescriptor beanDescriptor, BeanCreator<T> beanCreator) {
        CacheableBeanCreator<T> cacheableCreator = cacheable(beanCreator);
        Set<Class<?>> types = HierarchyIterator.getClassHierarchy(beanDescriptor.getType());
        types.forEach(type -> {
            List<CacheableBeanCreator<?>> registeredBeanCreators = beanCreators
                    .computeIfAbsent(beanDescriptor.withType(type), (k) -> new ArrayList<>());
            registeredBeanCreators.add(cacheableCreator);
        });
    }

    public Context buildEager() {
        Context context = new Context(beanCreators);
        beanCreators.forEach((descriptor, creator) -> {
            if (descriptor.getName() != null) {
                context.get(descriptor.getType(), descriptor.getName());
            } else {
                context.get(descriptor.getType());
            }
        });
        context.init();
        return context;
    }

    public Context build() {
        Context context = new Context(beanCreators);
        context.init();
        return context;
    }
}
