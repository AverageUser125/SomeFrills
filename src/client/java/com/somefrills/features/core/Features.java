package com.somefrills.features.core;

import com.somefrills.misc.ImmutableClassToInstanceMap;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class Features {
    private static volatile ImmutableClassToInstanceMap<AbstractFeature> FEATURES =
            ImmutableClassToInstanceMap.of();

    /**
     * Initialize all features in com.somefrills.features and subpackages
     */
    public static void init() {
        String packageName = "com.somefrills.features";

        try {
            List<Class<?>> classes = getClasses(packageName);

            ImmutableClassToInstanceMap.Builder<AbstractFeature> builder =
                    ImmutableClassToInstanceMap.builder();

            for (Class<?> clazz : classes) {
                // Skip anything that does NOT extend Feature
                if (!AbstractFeature.class.isAssignableFrom(clazz)) continue;

                // Skip abstract classes
                if (Modifier.isAbstract(clazz.getModifiers())) continue;

                try {
                    addFeature(builder, clazz.asSubclass(AbstractFeature.class));
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException(
                            "Feature class " + clazz.getName() + " must have a no-arg constructor", e
                    );
                }
            }

            // 🔒 Atomic publish (important for thread safety)
            FEATURES = builder.build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize features", e);
        }
    }

    private static <T extends AbstractFeature> void addFeature(
            ImmutableClassToInstanceMap.Builder<AbstractFeature> builder,
            Class<T> clazz) throws Exception {

        var constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);

        T instance = clazz.cast(constructor.newInstance());
        builder.put(clazz, instance);
    }

    /**
     * Get feature by class
     */
    public static <T extends Feature> T get(Class<T> featureClass) {
        return FEATURES.getInstance(featureClass);
    }

    /**
     * Helper to recursively list all classes in a package (filesystem + jar)
     */
    private static List<Class<?>> getClasses(String packageName) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(path);

        if (resource == null) return classes;

        String protocol = resource.getProtocol();

        if (protocol.equals("file")) {
            File directory = new File(resource.toURI());
            if (directory.exists()) {
                scanDirectory(packageName, directory, classes);
            }

        } else if (protocol.equals("jar")) {
            String jarPath = resource.getPath().substring(5, resource.getPath().indexOf("!"));
            try (java.util.jar.JarFile jar = new java.util.jar.JarFile(
                    java.net.URLDecoder.decode(jarPath, StandardCharsets.UTF_8))) {

                Enumeration<java.util.jar.JarEntry> entries = jar.entries();

                while (entries.hasMoreElements()) {
                    java.util.jar.JarEntry entry = entries.nextElement();
                    String name = entry.getName();

                    if (name.startsWith(path) && name.endsWith(".class") && !entry.isDirectory()) {
                        String className = name
                                .replace('/', '.')
                                .substring(0, name.length() - 6);

                        classes.add(Class.forName(className));
                    }
                }
            }
        }

        return classes;
    }

    /**
     * Recursively scan directory for .class files
     */
    private static void scanDirectory(String packageName, File directory, List<Class<?>> classes)
            throws ClassNotFoundException {

        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(packageName + "." + file.getName(), file, classes);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." +
                        file.getName().substring(0, file.getName().length() - 6);

                classes.add(Class.forName(className));
            }
        }
    }
}