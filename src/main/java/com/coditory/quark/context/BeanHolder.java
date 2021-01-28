package com.coditory.quark.context;

import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

final class BeanHolder<T> {
    static <T> BeanHolder<T> holder(BeanDescriptor<T> descriptor, BeanCreator<T> creator) {
        return new BeanHolder<>(descriptor, creator);
    }

    private final BeanCreator<T> creator;
    private final BeanDescriptor<T> descriptor;
    private final Set<Class<?>> classHierarchy;
    private T bean;

    private BeanHolder(BeanDescriptor<T> descriptor, BeanCreator<T> creator) {
        this.creator = requireNonNull(creator);
        this.descriptor = requireNonNull(descriptor);
        this.classHierarchy = HierarchyIterator.getClassHierarchy(descriptor.getType());
    }

    Class<T> getBeanType() {
        return descriptor.getType();
    }

    String getBeanName() {
        return descriptor.getName();
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
        return creator.isActive(context);
    }

    @Nullable
    public T get(ResolutionContext context) {
        if (bean == null) {
            createBean(context);
        }
        return bean;
    }

    private void createBean(ResolutionContext context) {
        bean = creator.create(context);
        if (bean == null) {
            throw new ContextException("Expected non-null bean: " + descriptor);
        }
    }
}
