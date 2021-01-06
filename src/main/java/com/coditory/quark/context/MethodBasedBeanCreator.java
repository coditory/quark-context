package com.coditory.quark.context;

import java.lang.reflect.Method;

import static java.util.Objects.requireNonNull;

class MethodBasedBeanCreator<T> implements BeanCreator<T> {
    private final Method method;
    private final BeanCreator<?> creator;

    public MethodBasedBeanCreator(BeanCreator<?> creator, Method method) {
        this.creator = requireNonNull(creator);
        this.method = requireNonNull(method);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T create(ResolutionContext context) {
        Object object = creator.create(context);
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] args = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; ++i) {
            Class<?> parameterType = parameterTypes[i];
            args[i] = context.get(parameterType);
        }
        try {
            return (T) method.invoke(object, args);
        } catch (Exception e) {
            throw new ContextException("Could no create bean from method: " + method, e);
        }
    }
}
