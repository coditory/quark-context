package com.coditory.quark.context;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.coditory.quark.context.BeanDescriptor.descriptor;
import static java.util.Objects.requireNonNull;

final class ResolutionPath {
    private static final ResolutionPath EMPTY = new ResolutionPath(List.of());

    static ResolutionPath emptyResolutionPath() {
        return EMPTY;
    }

    static ResolutionPath with(Class<?> type) {
        return with(type, null);
    }

    static ResolutionPath with(Class<?> type, String name) {
        return new ResolutionPath(List.of(descriptor(type, name)));
    }

    private final List<BeanDescriptor<?>> path;

    private ResolutionPath(List<BeanDescriptor<?>> path) {
        requireNonNull(path);
        this.path = List.copyOf(path);
    }

    boolean isEmpty() {
        return path.isEmpty();
    }

    boolean contains(Class<?> type, String name) {
        return path.contains(descriptor(type, name));
    }

    boolean contains(Class<?> type) {
        return contains(type, null);
    }

    ResolutionPath add(Class<?> type) {
        return add(type, null);
    }

    ResolutionPath add(BeanDescriptor<?> descriptor) {
        return add(descriptor.getType(), descriptor.getName());
    }

    ResolutionPath add(Class<?> type, String name) {
        BeanDescriptor<?> element = descriptor(type, name);
        List<BeanDescriptor<?>> newPath = new ArrayList<>(path);
        newPath.add(element);
        if (path.contains(element)) {
            String circle = newPath.stream()
                    .map(BeanDescriptor::toShortString)
                    .collect(Collectors.joining(" -> "));
            throw new CyclicDependencyException("Detected cyclic dependency: " + circle);
        }
        return new ResolutionPath(newPath);
    }
}
