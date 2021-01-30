package com.coditory.quark.context;

import com.coditory.quark.context.annotations.Bean;
import com.coditory.quark.context.annotations.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import static com.coditory.quark.context.Args.checkNonNull;
import static com.coditory.quark.context.BeanDescriptor.descriptor;
import static com.coditory.quark.context.BeanHolder.holder;
import static com.coditory.quark.context.ConstructorBasedBeanCreator.fromConstructor;

public final class ContextBuilder {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
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
        Configuration configuration = type.getAnnotation(Configuration.class);
        if (configuration != null) {
            if (type.isAnnotationPresent(Bean.class)) {
                throw new ContextException("Detected incompatible annotations on class: " + type.getCanonicalName()
                        + ". Class should have one annotation: @Configuration or @Bean.");
            }
            addBeansFromConfiguration(configuration, type);
        } else if (type.isAnnotationPresent(Bean.class)) {
            addAnnotatedBeanClass(type);
        }
        return this;
    }

    private <T> void addAnnotatedBeanClass(Class<T> type) {
        Bean annotation = type.getAnnotation(Bean.class);
        BeanCreator<T> creator = fromConstructor(type);
        addAnnotatedCreator(annotation, type, creator);
    }

    private <T> void addBeansFromConfiguration(Configuration configuration, Class<T> type) {
        String name = configuration.name().isBlank()
                ? configuration.value()
                : configuration.name();
        BeanHolder<T> holder = holder(descriptor(type, name), fromConstructor(type), configuration.eager());
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
        BeanHolder<T> holder = holder(descriptor(type, name), creator, annotation.eager());
        addBeanHolder(holder);
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
        BeanDescriptor<T> descriptor = descriptor((Class<T>) bean.getClass());
        BeanHolder<T> holder = holder(descriptor, (ctx) -> bean, true);
        addBeanHolder(holder);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> ContextBuilder add(T bean, String name) {
        checkNonNull(bean, "bean");
        checkNonNull(name, "name");
        BeanDescriptor<T> descriptor = descriptor((Class<T>) bean.getClass(), name);
        BeanHolder<T> holder = holder(descriptor, (ctx) -> bean, true);
        addBeanHolder(holder);
        return this;
    }

    public <T> ContextBuilder add(Class<T> type, Predicate<ConditionContext> condition, BeanCreator<T> beanCreator) {
        checkNonNull(condition, "condition");
        checkNonNull(type, "type");
        checkNonNull(beanCreator, "beanCreator");
        return add(type, wrapBeanCreator(condition, beanCreator));
    }

    public <T> ContextBuilder add(Class<T> type, String name, Predicate<ConditionContext> condition, BeanCreator<T> beanCreator) {
        checkNonNull(condition, "condition");
        checkNonNull(type, "type");
        checkNonNull(name, "name");
        checkNonNull(beanCreator, "beanCreator");
        return add(type, name, wrapBeanCreator(condition, beanCreator));
    }

    private <T> BeanCreator<T> wrapBeanCreator(Predicate<ConditionContext> condition, BeanCreator<T> beanCreator) {
        return new BeanCreator<T>() {
            @Override
            public T create(ResolutionContext context) {
                return beanCreator.create(context);
            }

            @Override
            public boolean isActive(ConditionContext context) {
                return beanCreator.isActive(context) && condition.test(context);
            }
        };
    }

    public <T> ContextBuilder add(Class<T> type, BeanCreator<T> beanCreator) {
        checkNonNull(beanCreator, "beanCreator");
        addBeanHolder(holder(descriptor(type), beanCreator));
        return this;
    }

    public <T> ContextBuilder add(Class<T> type, String name, BeanCreator<T> beanCreator) {
        checkNonNull(name, "name");
        checkNonNull(beanCreator, "beanCreator");
        addBeanHolder(holder(descriptor(type, name), beanCreator));
        return this;
    }

    private void addBeanHolder(BeanHolder<?> holder) {
        beanHolders.add(holder);
    }

    public Context buildEager() {
        return Context.createEager(beanHolders, properties);
    }

    public Context build() {
        return Context.create(beanHolders, properties);
    }
}
