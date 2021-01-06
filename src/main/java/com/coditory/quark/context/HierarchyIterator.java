package com.coditory.quark.context;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

class HierarchyIterator implements Iterator<Class<?>> {
    public static Set<Class<?>> getClassHierarchy(Class<?> forClass) {
        Iterable<Class<?>> iterable = () -> new HierarchyIterator(forClass);
        return StreamSupport
                .stream(iterable.spliterator(), false)
                .collect(Collectors.toSet());
    }

    private final Queue<Class<?>> remaining = new LinkedList<>();
    private final Set<Class<?>> visited = new LinkedHashSet<>();

    public HierarchyIterator(Class<?> initial) {
        append(initial);
    }

    private void append(Class<?> toAppend) {
        if (toAppend != null && !visited.contains(toAppend)) {
            remaining.add(toAppend);
            visited.add(toAppend);
        }
    }

    @Override
    public boolean hasNext() {
        return remaining.size() > 0;
    }

    @Override
    public Class<?> next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        Class<?> polled = remaining.poll();
        append(polled.getSuperclass());
        for (Class<?> superInterface : polled.getInterfaces()) {
            append(superInterface);
        }
        return polled;
    }
}
