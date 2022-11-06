package com.coditory.quark.context;

import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

public record BeanDescriptor<T>(Class<T> type, String name) {
    @NotNull
    public static <T> BeanDescriptor<T> descriptor(@NotNull Class<T> type) {
        return new BeanDescriptor<>(type, null);
    }

    @NotNull
    public static <T> BeanDescriptor<T> descriptor(@NotNull Class<T> type, String name) {
        return new BeanDescriptor<>(type, name);
    }

    public BeanDescriptor(@NotNull Class<T> type, String name) {
        this.type = requireNonNull(type);
        this.name = name == null || name.isBlank() ? null : name;
    }

    @NotNull
    public <R> BeanDescriptor<R> withType(@NotNull Class<R> type) {
        return new BeanDescriptor<>(type, name);
    }

    public boolean hasName() {
        return name != null;
    }

    @NotNull
    public String toShortString() {
        return name != null
                ? type.getSimpleName() + ":" + name
                : type.getSimpleName();
    }

    @Override
    public String toString() {
        return "BeanDescriptor{" + toShortString() + "}";
    }
}
