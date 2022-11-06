package com.coditory.quark.context;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.coditory.quark.context.BeanDescriptor.descriptor;
import static com.coditory.quark.context.Preconditions.expectNonNull;

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

    @NotNull
    ResolutionPath getResolutionPath() {
        return path;
    }

    @NotNull
    ResolutionContext withPath(ResolutionPath path) {
        return new ResolutionContext(context, path);
    }

    @NotNull
    public <T> T get(@NotNull Class<T> type) {
        expectNonNull(type, "type");
        return context.get(descriptor(type), path);
    }

    @Nullable
    public <T> T getOrNull(@NotNull Class<T> type) {
        expectNonNull(type, "type");
        return context.getOrNull(descriptor(type), path);
    }

    @NotNull
    public <T> T get(@NotNull Class<T> type, @NotNull String name) {
        expectNonNull(type, "type");
        expectNonNull(name, "name");
        return context.get(descriptor(type, name), path);
    }

    @Nullable
    public <T> T getOrNull(@NotNull Class<T> type, @NotNull String name) {
        expectNonNull(type, "type");
        expectNonNull(name, "name");
        return context.getOrNull(descriptor(type, name), path);
    }

    public boolean contains(@NotNull Class<?> type) {
        expectNonNull(type, "type");
        return context.contains(type);
    }

    public boolean contains(@NotNull String name) {
        expectNonNull(name, "name");
        return context.contains(name);
    }

    public boolean contains(@NotNull Class<?> type, @NotNull String name) {
        expectNonNull(type, "type");
        expectNonNull(name, "name");
        return context.contains(type, name);
    }

    @NotNull
    public <T> List<T> getAll(@NotNull Class<T> type) {
        expectNonNull(type, "type");
        return context.getAll(type, path);
    }

    @NotNull
    public <T> List<T> getAllOrEmpty(@NotNull Class<T> type) {
        expectNonNull(type, "type");
        return context.getAllOrEmpty(type, path);
    }
}
