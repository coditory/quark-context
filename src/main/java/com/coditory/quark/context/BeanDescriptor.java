package com.coditory.quark.context;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

final class BeanDescriptor<T> {
    public static <T> BeanDescriptor<T> descriptor(Class<T> type) {
        return new BeanDescriptor<>(type, null);
    }

    public static <T> BeanDescriptor<T> descriptor(Class<T> type, String name) {
        return new BeanDescriptor<>(type, name);
    }

    private final String name;
    private final Class<T> type;

    private BeanDescriptor(Class<T> type, String name) {
        this.type = requireNonNull(type);
        this.name = name == null || name.isBlank() ? null : name;
    }

    <R> BeanDescriptor<R> withType(Class<R> type) {
        return new BeanDescriptor<>(type, name);
    }

    String getName() {
        return name;
    }

    Class<T> getType() {
        return type;
    }

    @Override
    public String toString() {
        return "BeanDescriptor{" +
                "name='" + name + '\'' +
                ", type=" + type +
                '}';
    }

    String toShortString() {
        return name != null
                ? type.getSimpleName() + ":" + name
                : type.getSimpleName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BeanDescriptor<?> that = (BeanDescriptor<?>) o;
        return Objects.equals(name, that.name)
                && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }
}
