package com.coditory.quark.context;

import java.util.List;

import static com.coditory.quark.context.Args.checkNonNull;
import static com.coditory.quark.context.BeanDescriptor.descriptor;

public final class ResolutionContext {
    private final Context context;
    private final ResolutionPath path;

    ResolutionContext(
            Context context,
            ResolutionPath path
    ) {
        this.context = context;
        this.path = path;
    }

    public <T> T get(Class<T> type) {
        checkNonNull(type, "type");
        return context.get(descriptor(type), path);
    }

    public <T> T getOrNull(Class<T> type) {
        checkNonNull(type, "type");
        return context.getOrNull(descriptor(type), path);
    }

    public <T> T get(Class<T> type, String name) {
        checkNonNull(type, "type");
        checkNonNull(name, "name");
        return context.get(descriptor(type, name), path);
    }

    public <T> T getOrNull(Class<T> type, String name) {
        checkNonNull(type, "type");
        checkNonNull(name, "name");
        return context.getOrNull(descriptor(type, name), path);
    }

    public boolean contains(Class<?> type) {
        checkNonNull(type, "type");
        return context.contains(type);
    }

    public boolean contains(String name) {
        checkNonNull(name, "name");
        return context.contains(name);
    }

    public boolean contains(Class<?> type, String name) {
        checkNonNull(type, "type");
        checkNonNull(name, "name");
        return context.contains(type, name);
    }

    public <T> List<T> getAll(Class<T> type) {
        checkNonNull(type, "type");
        return context.getAll(type, path);
    }

    public <T> List<T> getAllOrEmpty(Class<T> type) {
        checkNonNull(type, "type");
        return context.getAllOrEmpty(type, path);
    }
}
