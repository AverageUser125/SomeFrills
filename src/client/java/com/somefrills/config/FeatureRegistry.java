package com.somefrills.config;

import com.somefrills.Main;
import com.somefrills.misc.Utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Scans the classpath for classes under com.somefrills.features.* that expose a public static
 * Feature `instance` field and collects their SettingGeneric fields. Used to auto-register
 * features and to build the UI dynamically.
 */
public class FeatureRegistry {
    public static final List<FeatureInfo> FEATURES = new ArrayList<>();

    public static void init() {
        FEATURES.clear();

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

                    // Validate event handler methods: any method annotated with @EventHandler must be static
                    for (Method m : cls.getDeclaredMethods()) {
                        if (m.isAnnotationPresent(meteordevelopment.orbit.EventHandler.class) && !Modifier.isStatic(m.getModifiers())) {
                            throw new IllegalStateException("Feature class '" + cls.getName() + "' declares a non-static @EventHandler method '" + m.getName() + "' — feature event handlers must be static when subscribing the class. Use a static method or have the registry subscribe an instance.");
                        }
                    }

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
                            info.settings.add(new SettingInfo(Utils.humanize(f.getName()), Utils.humanize(desc.value()), setting));
                        }
                    }

                    FEATURES.add(info);
                } catch (Throwable t) {
                    Main.LOGGER.debug("Failed to inspect feature class {}: {}", cls.getName(), t.toString());
                    // If it's an IllegalStateException we want it to bubble up so the developer notices
                    if (t instanceof IllegalStateException) throw (IllegalStateException) t;
                }
            }

            // NOTE: subscription/unsubscription of feature classes is intentionally NOT performed here.
            // The Config.save() method performs subscription reconciliation so that enabling/disabling
            // features only takes effect on explicit config saves, per user request.

            Main.LOGGER.info("FeatureRegistry: discovered {} feature(s)", FEATURES.size());
        } catch (IOException e) {
            Main.LOGGER.error("Error scanning features package", e);
        }
    }

    // Return the FeatureInfo for a given Feature instance, or null if not found
    public static FeatureInfo getInfoForFeature(Feature feature) {
        if (feature == null) return null;
        for (FeatureInfo info : FEATURES) {
            if (info.featureInstance == feature) return info;
        }
        return null;
    }

    // Subscribe a single feature's class on the event bus
    public static void subscribeFeature(Feature feature) {
        FeatureInfo info = getInfoForFeature(feature);
        if (info == null) return;
        try {
            Main.eventBus.subscribe(info.clazz);
        } catch (Throwable t) {
            Main.LOGGER.debug("Failed to subscribe feature class {}: {}", info.clazz.getName(), t.toString());
        }
    }

    public static void unsubscribeFeature(Feature feature) {
        FeatureInfo info = getInfoForFeature(feature);
        if (info == null) return;
        Main.eventBus.unsubscribe(info.clazz);
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
        for (String cn : classNames.stream().distinct().toList()) {
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

    public static void reconcileFeatureSubscriptions() {
        for (FeatureInfo info : FEATURES) {
            if (info.featureInstance.isActive()) {
                Main.eventBus.subscribe(info.clazz);
            } else {
                Main.eventBus.unsubscribe(info.clazz);
            }
        }
    }

    public static List<FeatureInfo> getFeatures() {
        return FEATURES;
    }

    public static class FeatureInfo {
        public final String name;
        public final String description;
        public final Class<?> clazz;
        public final Feature featureInstance;
        public final List<SettingInfo> settings = new ArrayList<>();

        public FeatureInfo(Class<?> clazz, Feature featureInstance) {
            this.clazz = clazz;
            this.featureInstance = featureInstance;
            name = Utils.humanize(featureInstance.key());
            // FIXME: ideally the description should be a separate field,
            //  but for now we can just reuse the key as the description until
            //  we have a better system for providing user-friendly descriptions
            description = Utils.humanize(featureInstance.key());
        }
    }

    public record SettingInfo(String name, String description, SettingGeneric settingInstance) {
    }
}
