package com.coditory.quark.context;

import com.coditory.quark.context.annotations.Close;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.lang.reflect.Method;

import static com.coditory.quark.context.DependencyResolver.resolveArguments;
import static com.coditory.quark.context.MethodBasedBeanCreator.simplifyMethodName;

final class BeanFinalizer {
    private static final Logger log = LoggerFactory.getLogger(BeanInitializer.class);

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
        Timer timer = Timer.start();
        try {
            Object[] args = resolveArguments(method, context);
            method.invoke(bean, args);
        } catch (Exception e) {
            if (e instanceof UnsupportedOperationException) {
                log.debug("Bean {} threw UnsupportedOperationException from {}", descriptor.toShortString(), simplifyMethodName(method));
            } else {
                throw new BeanFinalizationException("Could not close bean: " + descriptor.toShortString() + " using method: " + simplifyMethodName(method), e);
            }
        }
        log.debug("Closed bean {} using method {} in {}", descriptor.toShortString(), simplifyMethodName(method), timer.measureAndFormat());
    }

    private static void closeBean(Closeable bean, BeanDescriptor<?> descriptor) {
        Timer timer = Timer.start();
        try {
            bean.close();
        } catch (Exception e) {
            throw new BeanFinalizationException("Could not close bean: " + descriptor.toShortString(), e);
        }
        log.debug("Closed bean {} in {}", descriptor.toShortString(), timer.measureAndFormat());
    }
}
