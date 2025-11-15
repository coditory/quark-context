package com.coditory.quark.context;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

import static com.coditory.quark.context.DependencyResolver.resolveArguments;
import static java.util.Objects.requireNonNull;

final class MethodBasedBeanCreator<T> implements BeanCreator<T> {
    static String simplifyMethodName(Method method) {
        String params = Arrays.stream(method.getParameterTypes())
                .map(Class::getSimpleName)
                .collect(Collectors.joining(", "));
        return method.getName() + "(" + params + ")";
    }

    private final Method method;
    private final BeanHolder<?> holder;

    public MethodBasedBeanCreator(BeanHolder<?> holder, Method method) {
        this.holder = requireNonNull(holder);
        this.method = requireNonNull(method);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public T create(@NotNull ResolutionContext context) {
        try {
            Object object = holder.get(context);
            Object[] args = resolveArguments(method, context);
            return (T) method.invoke(object, args);
        } catch (Exception e) {
            throw new ContextException("Could not create bean from method: " + simplifyMethodName(method), e);
        }
    }

    @Override
    public boolean isActive(@NotNull ConditionContext context) {
        return holder.isActive(context) && ConditionsResolver.isActive(context, method);
    }
}
