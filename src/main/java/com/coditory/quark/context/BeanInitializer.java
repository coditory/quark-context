package com.coditory.quark.context;

import com.coditory.quark.context.annotations.Init;

import java.lang.reflect.Method;

import static com.coditory.quark.context.DependencyResolver.resolveArguments;

final class BeanInitializer {
    private BeanInitializer() {
        throw new UnsupportedOperationException("Do not instantiate utility class");
    }

    static void initializeBean(Object bean, ResolutionContext context) {
        if (bean instanceof Initializable) {
            initializeBean((Initializable) bean);
        }
        for (Method method : bean.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Init.class)) {
                method.setAccessible(true);
                initializeBean(bean, method, context);
            }
        }
    }

    private static void initializeBean(Object bean, Method method, ResolutionContext context) {
        Object[] args = resolveArguments(method, context);
        try {
            method.invoke(bean, args);
        } catch (Exception e) {
            throw new ContextException("Could not initialize bean using method: " + method, e);
        }
    }

    private static void initializeBean(Initializable bean) {
        try {
            bean.init();
        } catch (Exception e) {
            throw new ContextException("Could not initialize bean: " + bean.getClass().getCanonicalName(), e);
        }
    }
}
