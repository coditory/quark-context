package com.coditory.quark.context;

import com.coditory.quark.context.annotations.ConditionalOnBean;
import com.coditory.quark.context.annotations.ConditionalOnClass;
import com.coditory.quark.context.annotations.ConditionalOnDisabledProperty;
import com.coditory.quark.context.annotations.ConditionalOnMissingBean;
import com.coditory.quark.context.annotations.ConditionalOnMissingClass;
import com.coditory.quark.context.annotations.ConditionalOnProperty;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

final class ConditionsResolver {

    private ConditionsResolver() {
        throw new UnsupportedOperationException("Do not instantiate utility class");
    }

    static boolean isActive(ConditionContext context, Method method) {
        return isActive(context, method.getAnnotations());
    }

    static boolean isActive(ConditionContext context, Class<?> type) {
        return isActive(context, type.getAnnotations());
    }

    static boolean isActive(ConditionContext context, Annotation[] annotations) {
        return Arrays.stream(annotations)
                .allMatch(annotation -> isActive(context, annotation));
    }

    private static boolean isActive(ConditionContext context, Annotation annotation) {
        if (annotation instanceof ConditionalOnClass) {
            return isConditionActive((ConditionalOnClass) annotation);
        }
        if (annotation instanceof ConditionalOnMissingClass) {
            return isConditionActive((ConditionalOnMissingClass) annotation);
        }
        if (annotation instanceof ConditionalOnBean) {
            return isConditionActive(context, (ConditionalOnBean) annotation);
        }
        if (annotation instanceof ConditionalOnMissingBean) {
            return isConditionActive(context, (ConditionalOnMissingBean) annotation);
        }
        if (annotation instanceof ConditionalOnProperty) {
            return isConditionActive(context, (ConditionalOnProperty) annotation);
        }
        if (annotation instanceof ConditionalOnDisabledProperty) {
            return isConditionActive(context, (ConditionalOnDisabledProperty) annotation);
        }
        return true;
    }

    private static boolean isConditionActive(ConditionContext context, ConditionalOnProperty condition) {
        String[] names = condition.value().length == 0
                ? condition.name()
                : condition.value();
        return Arrays.stream(names)
                .allMatch(name -> {
                    Object property = context.getProperty(name);
                    return property == null
                            ? condition.matchIfMissing()
                            : property.toString().equals(condition.havingValue());
                });
    }

    private static boolean isConditionActive(ConditionContext context, ConditionalOnDisabledProperty condition) {
        return Arrays.stream(condition.value())
                .allMatch(name -> {
                    Object property = context.getProperty(name);
                    return property == null || property.toString().equals("false");
                });
    }

    private static boolean isConditionActive(ConditionalOnClass condition) {
        if (condition == null) {
            return true;
        }
        return Arrays.stream(condition.value())
                .allMatch(ConditionsResolver::hasClass);
    }

    private static boolean isConditionActive(ConditionalOnMissingClass condition) {
        return Arrays.stream(condition.value())
                .noneMatch(ConditionsResolver::hasClass);
    }

    private static boolean isConditionActive(ConditionContext context, ConditionalOnMissingBean condition) {
        Class<?>[] types = condition.value().length == 0
                ? condition.type()
                : condition.value();
        boolean hasNoneBeansByType = Arrays.stream(types)
                .noneMatch(context::hasBean);
        boolean hasNoneBeansByName = Arrays.stream(condition.name())
                .noneMatch(context::hasBean);
        return hasNoneBeansByType && hasNoneBeansByName;
    }

    private static boolean isConditionActive(ConditionContext context, ConditionalOnBean condition) {
        Class<?>[] types = condition.value().length == 0
                ? condition.type()
                : condition.value();
        boolean hasBeansByType = Arrays.stream(types)
                .allMatch(context::hasBean);
        boolean hasBeansByName = Arrays.stream(condition.name())
                .allMatch(context::hasBean);
        return hasBeansByType && hasBeansByName;
    }

    private static boolean hasClass(String canonicalName) {
        try {
            Class.forName(canonicalName);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
