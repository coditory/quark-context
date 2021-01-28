package com.coditory.quark.context;

import com.coditory.quark.context.annotations.Bean;
import com.coditory.quark.context.annotations.Configuration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.coditory.quark.context.Args.checkNonNull;
import static com.coditory.quark.context.BeanDescriptor.descriptor;
import static com.coditory.quark.context.BeanHolder.holder;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toUnmodifiableSet;

public final class ContextBuilder {
    private final Set<BeanHolder<?>> beanHolders = new LinkedHashSet<>();
    private final Map<String, Object> properties = new LinkedHashMap<>();

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
        BeanCreator<T> creator = ConstructorBasedBeanCreator.fromConstructor(type);
        addAnnotatedCreator(annotation, type, creator);
    }

    private <T> void addBeansFromConfiguration(Class<T> type) {
        BeanHolder<T> holder = holder(descriptor(type), ConstructorBasedBeanCreator.fromConstructor(type));
        addBeanHolder(holder);
        for (Method method : type.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Bean.class)) {
                method.setAccessible(true);
                addFromAnnotatedConfiguration(holder, method, method.getReturnType());
            }
        }
    }

    private <T> void addFromAnnotatedConfiguration(BeanHolder<?> holder, Method method, Class<T> returnType) {
        BeanCreator<T> creator = new MethodBasedBeanCreator<>(holder, method);
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

    public <T> ContextBuilder setProperty(String name, Object value) {
        checkNonNull(name, "name");
        checkNonNull(value, "value");
        properties.put(name, value);
        return this;
    }

    public <T> ContextBuilder setProperties(Map<String, Object> properties) {
        checkNonNull(properties, "properties");
        this.properties.putAll(properties);
        return this;
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
        addBeanHolder(holder(descriptor(type), beanCreator));
        return this;
    }

    public <T> ContextBuilder add(Class<T> type, String name, BeanCreator<T> beanCreator) {
        checkNonNull(type, "type");
        checkNonNull(name, "name");
        checkNonNull(beanCreator, "beanCreator");
        addBeanHolder(holder(descriptor(type, name), beanCreator));
        return this;
    }

    private void addBeanHolder(BeanHolder<?> holder) {
        beanHolders.add(holder);
    }

    public Context buildEager() {
        Map<BeanDescriptor<?>, List<BeanHolder<?>>> holders = resolveConditionalBeans();
        Context context = new Context(holders, properties);
        holders.forEach((descriptor, creator) -> {
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
        Context context = new Context(resolveConditionalBeans(), properties);
        context.init();
        return context;
    }

    private Map<BeanDescriptor<?>, List<BeanHolder<?>>> resolveConditionalBeans() {
        return ContextResolver.resolve(beanHolders, properties);
    }
}
