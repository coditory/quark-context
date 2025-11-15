package com.coditory.quark.context;

import com.coditory.quark.context.annotations.Inject;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import static com.coditory.quark.context.DependencyResolver.resolveArguments;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

final class ConstructorBasedBeanCreator<T> implements BeanCreator<T> {
    @SuppressWarnings("unchecked")
    static <T> ConstructorBasedBeanCreator<T> fromConstructor(Class<T> type) {
        Constructor<?>[] constructors = type.getDeclaredConstructors();
        Constructor<T> constructor;
        if (constructors.length == 1) {
            constructor = (Constructor<T>) constructors[0];
        } else {
            List<Constructor<T>> annotated = Arrays.stream(constructors)
                    .filter(c -> c.isAnnotationPresent(Inject.class))
                    .map(c -> (Constructor<T>) c)
                    .toList();
            if (annotated.size() == 1) {
                constructor = annotated.getFirst();
            } else if (annotated.size() > 1) {
                throw new ContextException("Expected single constructor annotated with @Inject in class: "
                        + type.getCanonicalName() + ". Got: " + annotated.size());
            } else {
                throw new ContextException("Located multiple constructors in a bean of type: "
                        + type.getCanonicalName() + ". Mark one with @Inject.");
            }
        }
        constructor.setAccessible(true);
        return new ConstructorBasedBeanCreator<>(type, constructor);
    }

    private final Class<T> type;
    private final Constructor<T> constructor;

    ConstructorBasedBeanCreator(Class<T> type, Constructor<T> constructor) {
        this.type = requireNonNull(type);
        this.constructor = requireNonNull(constructor);
    }

    @NotNull
    @Override
    public T create(@NotNull ResolutionContext context) {
        try {
            Object[] args = resolveArguments(constructor, context);
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new ContextException("Could not create bean from constructor: " + constructor, e);
        }
    }

    @Override
    public boolean isActive(@NotNull ConditionContext context) {
        return ConditionsResolver.isActive(context, type);
    }
}
