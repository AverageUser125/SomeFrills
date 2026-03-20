package com.somefrills.config;

import com.somefrills.Main;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * Scans the classpath for classes under com.somefrills.features.* that expose a public static
 * Feature `instance` field and collects their SettingGeneric fields. Used to auto-register
 * features and to build the UI dynamically.
 */
public class FeatureRegistry {
    public static final List<FeatureInfo> FEATURES = new ArrayList<>();

    public static class FeatureInfo {
        public final Class<?> clazz;
        public final Feature featureInstance;
        public final Map<Field, SettingGeneric> settings = new LinkedHashMap<>();
        public final Map<Field, String> descriptions = new LinkedHashMap<>();

        public FeatureInfo(Class<?> clazz, Feature featureInstance) {
            this.clazz = clazz;
            this.featureInstance = featureInstance;
        }
    }

    public static void init() {
        try {
            List<Class<?>> classes = getClasses("com.somefrills.features");
            for (Class<?> cls : classes) {
                try {
                    Field instanceField = null;
                    try {
                        instanceField = cls.getDeclaredField("instance");
                    } catch (NoSuchFieldException ignored) {
                    }
                    if (instanceField == null) continue;
                    if (!Feature.class.isAssignableFrom(instanceField.getType())) continue;
                    instanceField.setAccessible(true);
                    Feature feat = (Feature) instanceField.get(null);
                    if (feat == null) continue;
                    FeatureInfo info = new FeatureInfo(cls, feat);

                    for (Field f : cls.getDeclaredFields()) {
                        if (!SettingGeneric.class.isAssignableFrom(f.getType())) continue;
                        if (!java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                        f.setAccessible(true);
                        SettingGeneric setting = (SettingGeneric) f.get(null);
                        if (setting != null) {
                            // Enforce @SettingDescription presence
                            SettingDescription desc = f.getAnnotation(SettingDescription.class);
                            if (desc == null) {
                                throw new IllegalStateException("Missing @SettingDescription for setting '" + f.getName() + "' in feature " + cls.getName());
                            }
                            // override key: use field name as config key
                            setting.overrideKey(f.getName());
                            // Only override parent if the setting did not already specify one
                            String currentParent = setting.getParent();
                            if (currentParent == null || currentParent.isEmpty()) {
                                setting.overrideParent(feat.key());
                            }
                            info.settings.put(f, setting);
                            info.descriptions.put(f, desc.value());
                        }
                    }

                    FEATURES.add(info);
                } catch (Throwable t) {
                    Main.LOGGER.debug("Failed to inspect feature class {}: {}", cls.getName(), t.toString());
                }
            }

            // subscribe found feature classes to event bus
            for (FeatureInfo info : FEATURES) {
                try {
                    Main.eventBus.subscribe(info.clazz);
                } catch (Throwable t) {
                    Main.LOGGER.debug("Failed to subscribe feature {}: {}", info.clazz.getName(), t.toString());
                }
            }

            Main.LOGGER.info("FeatureRegistry: discovered {} feature(s)", FEATURES.size());
        } catch (IOException e) {
            Main.LOGGER.error("Error scanning features package", e);
        }
    }

    private static List<Class<?>> getClasses(String packageName) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<String> classNames = new ArrayList<>();

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            String protocol = resource.getProtocol();
            if (protocol.equals("file")) {
                File dir = new File(resource.getFile());
                if (dir.exists()) {
                    classNames.addAll(findClassesInDir(dir, packageName));
                }
            } else if (protocol.equals("jar")) {
                JarURLConnection conn = (JarURLConnection) resource.openConnection();
                try (JarFile jarFile = conn.getJarFile()) {
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        if (name.startsWith(path) && name.endsWith(".class") && !name.contains("$")) {
                            String cls = name.replace('/', '.').substring(0, name.length() - 6);
                            classNames.add(cls);
                        }
                    }
                }
            }
        }

        List<Class<?>> classes = new ArrayList<>();
        for (String cn : classNames.stream().distinct().collect(Collectors.toList())) {
            try {
                classes.add(Class.forName(cn));
            } catch (Throwable ignored) {
            }
        }
        return classes;
    }

    private static List<String> findClassesInDir(File dir, String packageName) {
        List<String> names = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files == null) return names;
        for (File file : files) {
            if (file.isDirectory()) {
                names.addAll(findClassesInDir(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class") && !file.getName().contains("$")) {
                names.add(packageName + "." + file.getName().substring(0, file.getName().length() - 6));
            }
        }
        return names;
    }

    public static List<FeatureInfo> getFeatures() {
        return Collections.unmodifiableList(FEATURES);
    }
}
