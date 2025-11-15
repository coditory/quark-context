package com.coditory.quark.context;

import com.coditory.quark.context.annotations.Dependency;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.List;

final class DependencyResolver {
    private static final Dependency DEFAULT_DEPENDENCY_ANNOTATION = new Dependency() {
        @Override
        public Class<? extends Annotation> annotationType() {
            return Dependency.class;
        }

        @Override
        public String value() {
            return "";
        }

        @Override
        public String name() {
            return "";
        }

        @Override
        public boolean optional() {
            return false;
        }
    };

    private DependencyResolver() {
        throw new UnsupportedOperationException("Do not instantiate utility class");
    }

    static Object[] resolveArguments(Constructor<?> constructor, ResolutionContext context) {
        return resolveArguments(
                context,
                constructor.getGenericParameterTypes(),
                constructor.getParameterTypes(),
                constructor.getParameterAnnotations()
        );
    }

    static Object[] resolveArguments(Method method, ResolutionContext context) {
        return resolveArguments(
                context,
                method.getGenericParameterTypes(),
                method.getParameterTypes(),
                method.getParameterAnnotations()
        );
    }

    private static Object[] resolveArguments(ResolutionContext context, Type[] genericTypes, Class<?>[] parameterTypes, Annotation[][] annotations) {
        Object[] args = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; ++i) {
            Dependency dependency = getDependencyAnnotation(annotations[i]);
            Class<?> parameterType = parameterTypes[i];
            Type genericType = genericTypes[i];
            args[i] = resolveDependency(context, dependency, parameterType, genericType);
        }
        return args;
    }

    private static Dependency getDependencyAnnotation(Annotation[] annotations) {
        return Arrays.stream(annotations)
                .filter(a -> Dependency.class == a.annotationType())
                .map(a -> (Dependency) a)
                .findFirst()
                .orElse(DEFAULT_DEPENDENCY_ANNOTATION);
    }

    private static Object resolveDependency(ResolutionContext context, Dependency dependency, Class<?> parameterType, Type genericType) {
        String name = dependency.name().isBlank()
                ? dependency.value()
                : dependency.name();
        boolean optional = dependency.optional();
        if (!name.isEmpty() && List.class == parameterType) {
            throw new ContextException("Detected named @Dependency for a list of dependencies. " +
                    "Dependency of list of beans should not be named");
        }
        if (List.class == parameterType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            Type type = parameterizedType.getActualTypeArguments()[0];
            if (type instanceof Class<?> listItemType) {
                return optional
                        ? context.getAllOrEmpty(listItemType)
                        : context.getAll(listItemType);
            }
            if (type instanceof WildcardType wildcardItemType) {
                if (wildcardItemType.getUpperBounds().length != 1) {
                    throw new IllegalArgumentException("Invalid number of upper bound arguments");
                }
                if (wildcardItemType.getLowerBounds().length != 0) {
                    throw new IllegalArgumentException("Unexpected lower bound type in list dependency");
                }
                Type upperBound = wildcardItemType.getUpperBounds()[0];
                if (upperBound instanceof Class<?> lowerBoundClass) {
                    return optional
                            ? context.getAllOrEmpty(lowerBoundClass)
                            : context.getAll(lowerBoundClass);
                }
                throw new IllegalArgumentException("Unexpected lower bound type: " + upperBound);
            }
            throw new ContextException("Invalid List generic type in dependency: " + name);
        }
        if (!name.isEmpty()) {
            return optional
                    ? context.getOrNull(parameterType, name)
                    : context.get(parameterType, name);
        }
        return optional
                ? context.getOrNull(parameterType)
                : context.get(parameterType);
    }
}
