package com.coditory.quark.context;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface BeanCreator<T> {
    @NotNull
    T create(@NotNull ResolutionContext context);

    default boolean isActive(@NotNull ConditionContext context) {
        return true;
    }
}
