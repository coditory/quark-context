package com.coditory.quark.context;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import static com.coditory.quark.context.DependencyResolver.resolveArguments;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

final class ConstructorBasedBeanCreator<T> implements BeanCreator<T> {
    @SuppressWarnings("unchecked")
    static <T> ConstructorBasedBeanCreator<T> fromConstructor(Class<T> type) {
        Constructor<?>[] constructors = type.getConstructors();
        Constructor<T> constructor;
        if (constructors.length == 1) {
            constructor = (Constructor<T>) constructors[0];
        } else {
            List<Constructor<T>> annotated = Arrays.stream(constructors)
                    .filter(c -> c.isAnnotationPresent(Inject.class))
                    .map(c -> (Constructor<T>) c)
                    .collect(toList());
            if (annotated.size() == 1) {
                constructor = annotated.get(0);
            } else if (annotated.size() > 1) {
                throw new ContextException("Expected single constructor annotated with @Inject in class: "
                        + type.getCanonicalName() + ". Got: " + annotated.size());
            } else {
                throw new ContextException("Located multiple constructors in a bean of type: "
                        + type.getCanonicalName() + ". Mark one with @Inject.");
            }
        }
        constructor.setAccessible(true);
        return new ConstructorBasedBeanCreator<>(constructor);
    }

    private final Constructor<T> constructor;

    ConstructorBasedBeanCreator(Constructor<T> constructor) {
        this.constructor = requireNonNull(constructor);
    }

    @Override
    public T create(ResolutionContext context) {
        Object[] args = resolveArguments(constructor, context);
        try {
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new ContextException("Could no create bean from constructor: " + constructor, e);
        }
    }
}
