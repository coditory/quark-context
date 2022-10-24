package com.coditory.quark.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.coditory.quark.context.BeanDescriptor.descriptor;
import static com.coditory.quark.context.Preconditions.expect;
import static com.coditory.quark.context.Preconditions.expectNonNull;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public final class ResolutionPath {
    private static final ResolutionPath EMPTY = new ResolutionPath(List.of());

    public static ResolutionPath emptyResolutionPath() {
        return EMPTY;
    }

    public static ResolutionPath of(Class<?>... types) {
        List<BeanDescriptor<?>> path = Arrays.stream(types)
                .map(BeanDescriptor::descriptor)
                .collect(toList());
        return new ResolutionPath(path);
    }

    public static ResolutionPath of(Class<?> type, String name) {
        return new ResolutionPath(List.of(descriptor(type, name)));
    }

    private final List<BeanDescriptor<?>> path;
    private final Map<BeanDescriptor<?>, Integer> indexes = new HashMap<>();

    private ResolutionPath(List<BeanDescriptor<?>> path) {
        requireNonNull(path);
        this.path = List.copyOf(path);
        int index = 0;
        for (BeanDescriptor<?> descriptor : path) {
            indexes.put(descriptor, index);
        }
    }

    public boolean isEmpty() {
        return path.isEmpty();
    }

    public boolean contains(Class<?> type) {
        return contains(type, null);
    }

    public boolean contains(Class<?> type, String name) {
        return contains(descriptor(type, name));
    }

    public boolean contains(BeanDescriptor<?> descriptor) {
        return indexes.containsKey(descriptor);
    }

    public int size() {
        return path.size();
    }

    public BeanDescriptor<?> first() {
        return path.isEmpty() ? null : path.get(0);
    }

    public BeanDescriptor<?> last() {
        return path.isEmpty() ? null : path.get(path.size() - 1);
    }

    public BeanDescriptor<?> get(int index) {
        expect(index < path.size(), "Expected index <= path.size()");
        expect(index >= 0, "Expected index >= 0");
        return path.get(index);
    }

    public BeanDescriptor<?> getParent(BeanDescriptor<?> descriptor) {
        expectNonNull(descriptor, "descriptor");
        Integer index = indexes.get(descriptor);
        return index == null ? null : path.get(index);
    }

    public ResolutionPath removeFirst() {
        if (path.isEmpty()) {
            return this;
        }
        return remove(path.get(0));
    }

    public ResolutionPath removeLast() {
        if (path.isEmpty()) {
            return this;
        }
        return remove(path.get(path.size() - 1));
    }

    public ResolutionPath remove(BeanDescriptor<?> descriptor) {
        List<BeanDescriptor<?>> newPath = new ArrayList<>(path);
        newPath.remove(descriptor);
        return new ResolutionPath(newPath);
    }

    public boolean startsWith(BeanDescriptor<?> descriptor) {
        return !path.isEmpty() && path.get(0).equals(descriptor);
    }

    public boolean endsWith(BeanDescriptor<?> descriptor) {
        return !path.isEmpty() && path.get(path.size() - 1).equals(descriptor);
    }

    public ResolutionPath add(Class<?> type) {
        return add(type, null);
    }

    public ResolutionPath add(BeanDescriptor<?> descriptor) {
        return add(descriptor.type(), descriptor.name());
    }

    public ResolutionPath add(Class<?> type, String name) {
        BeanDescriptor<?> element = descriptor(type, name);
        expect(!path.contains(element), "Duplicated element on resolution path");
        List<BeanDescriptor<?>> newPath = new ArrayList<>(path);
        newPath.add(element);
        return new ResolutionPath(newPath);
    }

    public String toPathAsString() {
        return toPathAsString(null);
    }

    public String toPathAsString(BeanDescriptor<?> descriptor) {
        List<BeanDescriptor<?>> newPath = this.path;
        if (descriptor != null) {
            newPath = new ArrayList<>(this.path);
            newPath.add(descriptor);
        }
        return newPath.stream()
                .map(BeanDescriptor::toShortString)
                .collect(joining(" -> "));
    }

    @Override
    public String toString() {
        return toPathAsString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResolutionPath that = (ResolutionPath) o;
        return path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}
