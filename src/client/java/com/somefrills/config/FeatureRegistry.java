package com.somefrills.config;

import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FeatureRegistry {

    private static final Map<Class<? extends Feature>, Feature> FEATURES = new HashMap<>();

    /** Initialize all features in com.somefrills.features and subpackages */
    public static void init() {
        String packageName = "com.somefrills.features";
        try {
            List<Class<?>> classes = getClasses(packageName);
            for (Class<?> clazz : classes) {
                // Skip anything that does NOT extend Feature
                if (!Feature.class.isAssignableFrom(clazz)) continue;

                // Skip abstract classes
                if (java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) continue;

                // Instantiate
                Feature feature = (Feature) clazz.getDeclaredConstructor().newInstance();
                FEATURES.put((Class<? extends Feature>) clazz, feature);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Get feature by class */
    @SuppressWarnings("unchecked")
    public static <T extends Feature> T getFeature(Class<T> featureClass) {
        return (T) FEATURES.get(featureClass);
    }

    /** Get all instantiated features */
    public static List<Feature> getAllFeatures() {
        return new ArrayList<>(FEATURES.values());
    }

    /** Helper to recursively list all classes in a package (filesystem only) */
    private static List<Class<?>> getClasses(String packageName) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');
        URL resource = Thread.currentThread().getContextClassLoader().getResource(path);
        if (resource == null) return classes;

        File directory = new File(resource.toURI());
        if (!directory.exists()) return classes;

        scanDirectory(packageName, directory, classes);
        return classes;
    }

    /** Recursively scan directory for .class files */
    private static void scanDirectory(String packageName, File directory, List<Class<?>> classes) throws ClassNotFoundException {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                scanDirectory(packageName + "." + file.getName(), file, classes);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().replace(".class", "");
                classes.add(Class.forName(className));
            }
        }
    }
}