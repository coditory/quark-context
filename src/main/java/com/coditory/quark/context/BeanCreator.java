package com.coditory.quark.context;

@FunctionalInterface
public interface BeanCreator<T> {
    T create(ResolutionContext context);
}
