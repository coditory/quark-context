package com.coditory.quark.context;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Predicate;

final class ClasspathScanner implements Iterator<Class<?>> {
    static ClasspathScanner scanPackageAndSubPackages(String packageName) {
        return scanPackageAndSubPackages(packageName, (name) -> true);
    }

    static ClasspathScanner scanPackageAndSubPackages(String packageName, Predicate<String> filter) {
        try {
            return new ClasspathScanner(getClasses(packageName, filter));
        } catch (IOException e) {
            throw new RuntimeException("Could not scan classpath", e);
        }
    }

    private static List<String> getClasses(String packageName, Predicate<String> filter)
            throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<String> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName, filter));
        }
        return classes;
    }

    private static List<String> findClasses(File directory, String packageName, Predicate<String> filter) {
        List<String> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        if (files == null) {
            return classes;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName(), filter));
            } else if (file.getName().endsWith(".class")) {
                String canonicalName = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                if (filter.test(canonicalName)) {
                    classes.add(canonicalName);
                }
            }
        }
        return classes;
    }

    private final ClassLoader classLoader;
    private final Queue<String> classesToScan;

    ClasspathScanner(List<String> classesToScan) {
        this.classLoader = Thread.currentThread().getContextClassLoader();
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
