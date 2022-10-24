package com.coditory.quark.context;

import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.coditory.quark.context.BeanDescriptor.descriptor;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

class ConditionContext {
    static ConditionContext from(Set<BeanHolder<?>> holders, Map<String, Object> properties) {
        Set<BeanDescriptor<?>> descriptors = holders.stream()
                .map(BeanHolder::getDescriptor)
                .collect(toSet());
        return new ConditionContext(descriptors, properties);
    }

    static ConditionContext from(Map<String, Object> properties) {
        return new ConditionContext(Set.of(), properties);
    }

    private final Set<BeanDescriptor<?>> descriptors;
    private final Set<String> names;
    private final Map<String, Object> properties;

    private ConditionContext(Set<BeanDescriptor<?>> descriptors, Map<String, Object> properties) {
        this.descriptors = requireNonNull(descriptors);
        this.properties = requireNonNull(properties);
        this.names = descriptors.stream()
                .map(BeanDescriptor::name)
                .collect(toSet());
    }

    public boolean hasBean(Class<?> type, String name) {
        return descriptors.contains(descriptor(type, name));
    }

    public boolean hasBean(String name) {
        return names.contains(name);
    }

    public boolean hasBean(Class<?> type) {
        return descriptors.contains(descriptor(type));
    }

    @Nullable
    public Object getProperty(String name) {
        return properties.get(name);
    }

    public Object getPropertyOrDefault(String name, Object defaultValue) {
        return properties.getOrDefault(name, defaultValue);
    }

    ConditionContext with(BeanHolder<?> holder) {
        Set<BeanDescriptor<?>> descriptors = new HashSet<>(this.descriptors);
        descriptors.addAll(holder.getBeanClassHierarchyDescriptors());
        return new ConditionContext(descriptors, properties);
    }
}
