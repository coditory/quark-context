package com.coditory.quark.context;

import com.coditory.quark.context.annotations.Init;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

import static com.coditory.quark.context.DependencyResolver.resolveArguments;
import static com.coditory.quark.context.MethodBasedBeanCreator.simplifyMethodName;

final class BeanInitializer {
    private static final Logger log = LoggerFactory.getLogger(BeanInitializer.class);

    private BeanInitializer() {
        throw new UnsupportedOperationException("Do not instantiate utility class");
    }

    static void initializeBean(Object bean, BeanDescriptor<?> descriptor, ResolutionContext context) {
        for (Method method : bean.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Init.class)) {
                method.setAccessible(true);
                initializeBean(bean, descriptor, method, context);
            }
        }
    }

    private static void initializeBean(Object bean, BeanDescriptor<?> descriptor, Method method, ResolutionContext context) {
        Timer timer = Timer.start();
        try {
            Object[] args = resolveArguments(method, context);
            method.invoke(bean, args);
        } catch (Exception e) {
            throw new BeanInitializationException("Could not initialize bean: " + descriptor.toShortString() + " using method: " + simplifyMethodName(method), e);
        }
        log.debug("Initialized bean {} using method {} in {}", descriptor.toShortString(), simplifyMethodName(method), timer.measureAndFormat());
    }
}
