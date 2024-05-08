package com.coditory.quark.context;

import com.coditory.quark.context.annotations.PostInit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

import static com.coditory.quark.context.DependencyResolver.resolveArguments;
import static com.coditory.quark.context.MethodBasedBeanCreator.simplifyMethodName;

final class BeanPostInitializer {
    private static final Logger log = LoggerFactory.getLogger(BeanPostInitializer.class);

    private BeanPostInitializer() {
        throw new UnsupportedOperationException("Do not instantiate utility class");
    }

    static void postInitializeBean(Object bean, BeanDescriptor<?> descriptor, ResolutionContext context) {
        if (bean instanceof PostInitializable) {
            postInitializeBean((PostInitializable) bean, descriptor);
        }
        for (Method method : bean.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(PostInit.class)) {
                method.setAccessible(true);
                postInitializeBean(bean, descriptor, method, context);
            }
        }
    }

    private static void postInitializeBean(Object bean, BeanDescriptor<?> descriptor, Method method, ResolutionContext context) {
        Timer timer = Timer.start();
        try {
            Object[] args = resolveArguments(method, context);
            method.invoke(bean, args);
        } catch (Exception e) {
            throw new BeanInitializationException("Could not post initialize bean: " + descriptor.toShortString() + " using method: " + simplifyMethodName(method), e);
        }
        log.debug("Post initialized bean {} using method {} in {}", descriptor.toShortString(), simplifyMethodName(method), timer.measureAndFormat());
    }

    private static void postInitializeBean(PostInitializable bean, BeanDescriptor<?> descriptor) {
        Timer timer = Timer.start();
        try {
            bean.postInit();
        } catch (Exception e) {
            throw new BeanInitializationException("Could not post initialize bean: " + descriptor.toShortString(), e);
        }
        log.debug("Post initialized bean {} in {}", descriptor.toShortString(), timer.measureAndFormat());
    }
}
