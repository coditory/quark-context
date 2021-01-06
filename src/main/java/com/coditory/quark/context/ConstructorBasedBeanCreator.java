package com.coditory.quark.context;

import java.lang.reflect.Constructor;

import static java.util.Objects.requireNonNull;

class ConstructorBasedBeanCreator<T> implements BeanCreator<T> {
    @SuppressWarnings("unchecked")
    static <T> ConstructorBasedBeanCreator<T> fromConstructor(Class<T> clazz) {
        Constructor<?>[] constructors = clazz.getConstructors();
        if (constructors.length != 1) {
            throw new ContextException("Expected single constructor for class" + clazz.getCanonicalName());
        }
        Constructor<T> constructor = (Constructor<T>) constructors[0];
        constructor.setAccessible(true);
        return new ConstructorBasedBeanCreator<>(constructor);
    }

    private final Constructor<T> constructor;

    ConstructorBasedBeanCreator(Constructor<T> constructor) {
        this.constructor = requireNonNull(constructor);
    }

    @Override
    public T create(ResolutionContext context) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Object[] args = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; ++i) {
            Class<?> parameterType = parameterTypes[i];
            args[i] = context.get(parameterType);
        }
        try {
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new ContextException("Could no create bean from constructor: " + constructor, e);
        }
    }
}
