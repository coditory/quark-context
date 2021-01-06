package com.coditory.quark.context;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableList;

public class ResolutionContext {
    private final Context context;
    private final List<ResolutionElement> resolutionPath;

    static ResolutionContext start(Context context, Class<?> type, String name) {
        return new ResolutionContext(context, List.of(new ResolutionElement(type, name)));
    }

    ResolutionContext(
            Context context,
            List<ResolutionElement> resolutionPath
    ) {
        this.context = context;
        this.resolutionPath = resolutionPath;
    }

    private List<ResolutionElement> nextPath(Class<?> type) {
        return nextPath(type, null);
    }

    private List<ResolutionElement> nextPath(Class<?> type, String name) {
        ResolutionElement element = new ResolutionElement(type, name);
        List<ResolutionElement> path = new ArrayList<>(resolutionPath);
        path.add(element);
        if (resolutionPath.contains(element)) {
            String circle = path.stream()
                    .map(ResolutionElement::toShortString)
                    .collect(Collectors.joining(" -> "));
            throw new ContextException("Detected circular dependency: " + circle);
        }
        return unmodifiableList(path);
    }

    public <T> T get(Class<T> clazz) {
        return context.get(clazz, nextPath(clazz));
    }

    public <T> T getOrNull(Class<T> clazz) {
        return context.getOrNull(clazz, nextPath(clazz));
    }

    public <T> T get(String name, Class<T> clazz) {
        return context.get(clazz, nextPath(clazz, name));
    }

    public <T> T getOrNull(String name, Class<T> clazz) {
        return context.getOrNull(clazz, nextPath(clazz, name));
    }

    public boolean contains(Class<?> clazz) {
        return context.contains(clazz);
    }

    public boolean contains(String name) {
        return context.contains(name);
    }

    public <T> List<T> getAll(Class<T> clazz) {
        return context.getAll(clazz, nextPath(clazz));
    }

    public <T> List<T> getAllOrEmpty(Class<T> clazz) {
        return context.getAllOrEmpty(clazz, nextPath(clazz));
    }
}
