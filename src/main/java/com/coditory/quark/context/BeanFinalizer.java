package com.coditory.quark.context;

import java.io.Closeable;
import java.lang.reflect.Method;

import static com.coditory.quark.context.DependencyResolver.resolveArguments;

final class BeanFinalizer {
    private BeanFinalizer() {
        throw new UnsupportedOperationException("Do not instantiate utility class");
    }

    static void closeBean(Object bean, ResolutionContext context) {
        if (bean instanceof Closeable) {
            closeBean((Closeable) bean);
        }
        for (Method method : bean.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Close.class)) {
                method.setAccessible(true);
                closeBean(bean, method, context);
            }
        }
    }

    private static void closeBean(Object bean, Method method, ResolutionContext context) {
        Object[] args = resolveArguments(method, context);
        try {
            method.invoke(bean, args);
        } catch (Exception e) {
            throw new ContextException("Could not close bean using method: " + method, e);
        }
    }

    private static void closeBean(Closeable bean) {
        try {
            bean.close();
        } catch (Exception e) {
            throw new ContextException("Could not close bean: " + bean.getClass().getCanonicalName(), e);
        }
    }
}
