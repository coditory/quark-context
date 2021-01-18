package com.coditory.quark.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
        return new ResolutionPath(List.of(new ResolutionElement(type, name)));
    }

    private final List<ResolutionElement> path;

    private ResolutionPath(List<ResolutionElement> path) {
        requireNonNull(path);
        this.path = List.copyOf(path);
    }

    boolean isEmpty() {
        return path.isEmpty();
    }

    boolean contains(Class<?> type, String name) {
        return path.contains(new ResolutionElement(type, name));
    }

    boolean contains(Class<?> type) {
        return contains(type, null);
    }

    ResolutionPath add(Class<?> type) {
        return add(type, null);
    }

    ResolutionPath add(Class<?> type, String name) {
        ResolutionElement element = new ResolutionElement(type, name);
        List<ResolutionElement> newPath = new ArrayList<>(path);
        newPath.add(element);
        if (path.contains(element)) {
            String circle = newPath.stream()
                    .map(ResolutionElement::toShortString)
                    .collect(Collectors.joining(" -> "));
            throw new ContextException("Detected circular dependency: " + circle);
        }
        return new ResolutionPath(newPath);
    }

    private static final class ResolutionElement {
        private final Class<?> type;
        private final String name;

        ResolutionElement(Class<?> type, String name) {
            this.type = requireNonNull(type);
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ResolutionElement that = (ResolutionElement) o;
            return Objects.equals(type, that.type) && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, name);
        }

        public String toShortString() {
            String result = type.getSimpleName();
            if (name != null) {
                result += " (" + name + ")";
            }
            return result;
        }

        @Override
        public String toString() {
            return "ResolutionElement{" +
                    "type=" + type +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}
