package com.coditory.quark.context;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Predicate;

final class ClassPathScanner implements Iterator<Class<?>> {
    static ClassPathScanner scanPackageAndSubPackages(String packageName, Predicate<String> filter, ClassLoader classLoader) {
        try {
            List<String> classes = getClasses(packageName, filter, classLoader);
            return new ClassPathScanner(classes, classLoader);
        } catch (IOException e) {
            throw new RuntimeException("Could not scan classpath", e);
        }
    }

    private static List<String> getClasses(String packageName, Predicate<String> filter, ClassLoader classLoader)
            throws IOException {
        return ClassPath.from(classLoader)
                .getTopLevelClassesRecursive(packageName)
                .stream()
                .map(ClassPath.ClassInfo::getName)
                .filter(filter)
                .toList();
    }

    private final ClassLoader classLoader;
    private final Queue<String> classesToScan;

    ClassPathScanner(List<String> classesToScan, ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.classesToScan = new LinkedList<>(classesToScan);
    }

    @Override
    public boolean hasNext() {
        return !classesToScan.isEmpty();
    }

    @Override
    public Class<?> next() {
        String className = classesToScan.poll();
        return loadClass(className);
    }

    private Class<?> loadClass(String canonicalName) {
        try {
            return classLoader.loadClass(canonicalName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not load class: " + canonicalName, e);
        }
    }
}
