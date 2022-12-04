package com.coditory.quark.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

final class ClassPath {
    private static final Logger logger = LoggerFactory.getLogger(ClassPath.class.getName());
    private static final String CLASS_FILE_NAME_EXTENSION = ".class";
    private static final String PATH_SEPARATOR_SYS_PROP = System.getProperty("path.separator");
    private static final String JAVA_CLASS_PATH_SYS_PROP = System.getProperty("java.class.path");

    private final Set<ResourceInfo> resources;

    private ClassPath(Set<ResourceInfo> resources) {
        this.resources = resources;
    }

    public static ClassPath from(ClassLoader classloader) throws IOException {
        requireNonNull(classloader);
        Set<LocationInfo> locations = locationsFrom(classloader);
        Set<File> scanned = new LinkedHashSet<>();
        for (LocationInfo location : locations) {
            scanned.add(location.file());
        }
        Set<ResourceInfo> resources = new LinkedHashSet<>();
        for (LocationInfo location : locations) {
            resources.addAll(location.scanResources(scanned));
        }
        return new ClassPath(resources);
    }

    public Set<ClassInfo> getTopLevelClasses() {
        return resources.stream()
                .filter(r -> r instanceof ClassInfo)
                .map(r -> (ClassInfo) r)
                .filter(ClassInfo::isTopLevel)
                .collect(toSet());
    }

    public Set<ClassInfo> getTopLevelClassesRecursive(String packageName) {
        requireNonNull(packageName);
        String packagePrefix = packageName + '.';
        Set<ClassInfo> classes = new LinkedHashSet<>();
        for (ClassInfo classInfo : getTopLevelClasses()) {
            if (classInfo.getName().startsWith(packagePrefix)) {
                classes.add(classInfo);
            }
        }
        return unmodifiableSet(classes);
    }

    public static class ResourceInfo {
        private final File file;
        private final String resourceName;

        final ClassLoader loader;

        static ResourceInfo of(File file, String resourceName, ClassLoader loader) {
            return resourceName.endsWith(CLASS_FILE_NAME_EXTENSION)
                    ? new ClassInfo(file, resourceName, loader)
                    : new ResourceInfo(file, resourceName, loader);
        }

        ResourceInfo(File file, String resourceName, ClassLoader loader) {
            this.file = requireNonNull(file);
            this.resourceName = requireNonNull(resourceName);
            this.loader = requireNonNull(loader);
        }

        public File getFile() {
            return file;
        }

        @Override
        public int hashCode() {
            return resourceName.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ResourceInfo that) {
                return resourceName.equals(that.resourceName)
                        && loader == that.loader;
            }
            return false;
        }

        @Override
        public String toString() {
            return resourceName;
        }
    }

    public static final class ClassInfo extends ResourceInfo {
        private final String className;

        ClassInfo(File file, String resourceName, ClassLoader loader) {
            super(file, resourceName, loader);
            this.className = getClassName(resourceName);
        }

        public String getName() {
            return className;
        }

        public boolean isTopLevel() {
            return className.indexOf('$') == -1;
        }

        @Override
        public String toString() {
            return className;
        }
    }

    static Set<LocationInfo> locationsFrom(ClassLoader classloader) {
        Set<LocationInfo> locations = new LinkedHashSet<>();
        for (Map.Entry<File, ClassLoader> entry : getClassPathEntries(classloader).entrySet()) {
            locations.add(new LocationInfo(entry.getKey(), entry.getValue()));
        }
        return unmodifiableSet(locations);
    }

    static final class LocationInfo {
        final File home;
        private final ClassLoader classloader;

        LocationInfo(File home, ClassLoader classloader) {
            this.home = requireNonNull(home);
            this.classloader = requireNonNull(classloader);
        }

        public File file() {
            return home;
        }

        public Set<ResourceInfo> scanResources(Set<File> scannedFiles) throws IOException {
            Set<ResourceInfo> resources = new LinkedHashSet<>();
            scannedFiles.add(home);
            scan(home, scannedFiles, resources);
            return unmodifiableSet(resources);
        }

        private void scan(File file, Set<File> scannedUris, Set<ResourceInfo> result)
                throws IOException {
            try {
                if (!file.exists()) {
                    return;
                }
            } catch (SecurityException e) {
                logger.warn("Cannot access " + file + ": " + e);
                return;
            }
            if (file.isDirectory()) {
                scanDirectory(file, result);
            } else {
                scanJar(file, scannedUris, result);
            }
        }

        private void scanJar(File file, Set<File> scannedUris, Set<ResourceInfo> result) throws IOException {
            JarFile jarFile;
            try {
                jarFile = new JarFile(file);
            } catch (IOException e) {
                // Not a jar file
                return;
            }
            try {
                for (File path : getClassPathFromManifest(file, jarFile.getManifest())) {
                    // We only scan each file once independent of the classloader that file might be
                    // associated with.
                    if (scannedUris.add(path.getCanonicalFile())) {
                        scan(path, scannedUris, result);
                    }
                }
                scanJarFile(jarFile, result);
            } finally {
                try {
                    jarFile.close();
                } catch (IOException ignored) { // similar to try-with-resources, but don't fail scanning
                }
            }
        }

        private void scanJarFile(JarFile file, Set<ResourceInfo> result) {
            Enumeration<JarEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory() || entry.getName().equals(JarFile.MANIFEST_NAME)) {
                    continue;
                }
                result.add(ResourceInfo.of(new File(file.getName()), entry.getName(), classloader));
            }
        }

        private void scanDirectory(File directory, Set<ResourceInfo> result)
                throws IOException {
            Set<File> currentPath = new HashSet<>();
            currentPath.add(directory.getCanonicalFile());
            scanDirectory(directory, "", currentPath, result);
        }

        private void scanDirectory(
                File directory,
                String packagePrefix,
                Set<File> currentPath,
                Set<ResourceInfo> builder
        ) throws IOException {
            File[] files = directory.listFiles();
            if (files == null) {
                logger.warn("Cannot read directory " + directory);
                // IO error, just skip the directory
                return;
            }
            for (File f : files) {
                String name = f.getName();
                if (f.isDirectory()) {
                    File deref = f.getCanonicalFile();
                    if (currentPath.add(deref)) {
                        scanDirectory(deref, packagePrefix + name + "/", currentPath, builder);
                        currentPath.remove(deref);
                    }
                } else {
                    String resourceName = packagePrefix + name;
                    if (!resourceName.equals(JarFile.MANIFEST_NAME)) {
                        builder.add(ResourceInfo.of(f, resourceName, classloader));
                    }
                }
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof LocationInfo that) {
                return home.equals(that.home) && classloader.equals(that.classloader);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return home.hashCode();
        }

        @Override
        public String toString() {
            return home.toString();
        }
    }

    static Set<File> getClassPathFromManifest(File jarFile, Manifest manifest) {
        if (manifest == null) {
            return Set.of();
        }
        Set<File> result = new LinkedHashSet<>();
        String classpathAttribute = manifest
                .getMainAttributes()
                .getValue(Attributes.Name.CLASS_PATH.toString());
        if (classpathAttribute != null) {
            for (String path : classpathAttribute.split(" ")) {
                if (path.isBlank()) {
                    continue;
                }
                URL url;
                try {
                    url = getClassPathEntry(jarFile, path);
                } catch (MalformedURLException e) {
                    // Ignore bad entry
                    logger.warn("Invalid Class-Path entry: " + path);
                    continue;
                }
                if (url.getProtocol().equals("file")) {
                    result.add(toFile(url));
                }
            }
        }
        return unmodifiableSet(result);
    }

    static Map<File, ClassLoader> getClassPathEntries(ClassLoader classloader) {
        LinkedHashMap<File, ClassLoader> entries = new LinkedHashMap<>();
        // Search parent first, since it's the order ClassLoader#loadClass() uses.
        ClassLoader parent = classloader.getParent();
        if (parent != null) {
            entries.putAll(getClassPathEntries(parent));
        }
        for (URL url : getClassLoaderUrls(classloader)) {
            if (url.getProtocol().equals("file")) {
                File file = toFile(url);
                if (!entries.containsKey(file)) {
                    entries.put(file, classloader);
                }
            }
        }
        return unmodifiableMap(entries);
    }

    private static List<URL> getClassLoaderUrls(ClassLoader classloader) {
        if (classloader instanceof URLClassLoader) {
            return Arrays.asList(((URLClassLoader) classloader).getURLs());
        }
        if (classloader.equals(ClassLoader.getSystemClassLoader())) {
            return parseJavaClassPath();
        }
        return List.of();
    }

    private static List<URL> parseJavaClassPath() {
        List<URL> urls = new ArrayList<>();
        for (String entry : JAVA_CLASS_PATH_SYS_PROP.split(PATH_SEPARATOR_SYS_PROP)) {
            try {
                try {
                    urls.add(new File(entry).toURI().toURL());
                } catch (SecurityException e) { // File.toURI checks to see if the file is a directory
                    urls.add(new URL("file", null, new File(entry).getAbsolutePath()));
                }
            } catch (MalformedURLException e) {
                logger.warn("Malformed classpath entry: " + entry, e);
            }
        }
        return unmodifiableList(urls);
    }

    private static URL getClassPathEntry(File jarFile, String path) throws MalformedURLException {
        return new URL(jarFile.toURI().toURL(), path);
    }

    private static String getClassName(String filename) {
        int classNameEnd = filename.length() - CLASS_FILE_NAME_EXTENSION.length();
        return filename.substring(0, classNameEnd).replace('/', '.');
    }

    private static File toFile(URL url) {
        try {
            return new File(url.toURI()); // Accepts escaped characters like %20.
        } catch (URISyntaxException e) { // URL.toURI() doesn't escape chars.
            return new File(url.getPath()); // Accepts non-escaped chars like space.
        }
    }
}
