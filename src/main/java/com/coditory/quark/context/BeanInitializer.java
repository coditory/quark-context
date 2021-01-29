package com.coditory.quark.context;

import com.coditory.quark.context.annotations.Init;

import java.lang.reflect.Method;

import static com.coditory.quark.context.DependencyResolver.resolveArguments;

final class BeanInitializer {
    private BeanInitializer() {
        throw new UnsupportedOperationException("Do not instantiate utility class");
    }

    static void initializeBean(Object bean, BeanDescriptor<?> descriptor, ResolutionContext context) {
        if (bean instanceof Initializable) {
            initializeBean((Initializable) bean, descriptor);
        }
        for (Method method : bean.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Init.class)) {
                method.setAccessible(true);
                initializeBean(bean, descriptor, method, context);
            }
        }
    }

    private static void initializeBean(Object bean, BeanDescriptor<?> descriptor, Method method, ResolutionContext context) {
        try {
            Object[] args = resolveArguments(method, context);
            method.invoke(bean, args);
        } catch (Exception e) {
            throw new BeanInitializationException("Could not initialize bean: " + descriptor.toShortString() + " using method: " + method, e);
        }
    }

    private static void initializeBean(Initializable bean, BeanDescriptor<?> descriptor) {
        try {
            bean.init();
        } catch (Exception e) {
            throw new BeanInitializationException("Could not initialize bean: " + descriptor.toShortString(), e);
        }
    }
}
