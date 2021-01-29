package com.coditory.quark.context;

import com.coditory.quark.context.annotations.Close;

import java.io.Closeable;
import java.lang.reflect.Method;

import static com.coditory.quark.context.DependencyResolver.resolveArguments;

final class BeanFinalizer {
    private BeanFinalizer() {
        throw new UnsupportedOperationException("Do not instantiate utility class");
    }

    static void closeBean(Object bean, BeanDescriptor<?> descriptor, ResolutionContext context) {
        if (bean instanceof Closeable) {
            closeBean((Closeable) bean, descriptor);
        }
        for (Method method : bean.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Close.class)) {
                method.setAccessible(true);
                closeBean(bean, descriptor, method, context);
            }
        }
    }

    private static void closeBean(Object bean, BeanDescriptor<?> descriptor, Method method, ResolutionContext context) {
        try {
            Object[] args = resolveArguments(method, context);
            method.invoke(bean, args);
        } catch (Exception e) {
            throw new BeanFinalizationException("Could not close bean: " + descriptor.toShortString() + " using method: " + method, e);
        }
    }

    private static void closeBean(Closeable bean, BeanDescriptor<?> descriptor) {
        try {
            bean.close();
        } catch (Exception e) {
            throw new BeanFinalizationException("Could not close bean: " + descriptor.toShortString(), e);
        }
    }
}
