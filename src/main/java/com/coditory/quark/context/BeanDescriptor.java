package com.coditory.quark.context;

import static java.util.Objects.requireNonNull;

public record BeanDescriptor<T>(Class<T> type, String name) {
    public static <T> BeanDescriptor<T> descriptor(Class<T> type) {
        return new BeanDescriptor<>(type, null);
    }

    public static <T> BeanDescriptor<T> descriptor(Class<T> type, String name) {
        return new BeanDescriptor<>(type, name);
    }

    public BeanDescriptor(Class<T> type, String name) {
        this.type = requireNonNull(type);
        this.name = name == null || name.isBlank() ? null : name;
    }

    public <R> BeanDescriptor<R> withType(Class<R> type) {
        return new BeanDescriptor<>(type, name);
    }

    public boolean hasName() {
        return name != null;
    }

    public String toShortString() {
        return name != null
                ? type.getSimpleName() + ":" + name
                : type.getSimpleName();
    }

    @Override
    public String toString() {
        return name != null
                ? type.getSimpleName() + ":" + name
                : type.getSimpleName();
    }
}
