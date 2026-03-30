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

                    // Infer and set feature metadata (key, name, description) here —
                    // keep all reflection/derivation logic inside the registry.
                    String simpleName = cls.getSimpleName();
                    if (simpleName.isEmpty()) simpleName = "feature";
                    // config key: class name with lower-cased first char
                    String key = Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
                    feat.overrideKey(key);
                    // display name: humanized class name
                    feat.setName(Utils.humanize(simpleName));
                    if (feat.description() == null || feat.description().isEmpty()) {
                        feat.setDescription(deriveDescriptionFromName(simpleName));
                    }

                    // Create the FeatureInfo that will hold metadata and discovered settings
                    FeatureInfo info = new FeatureInfo(cls, feat);

                    // Validate event handler methods: any method annotated with @EventHandler must be static
                    for (Method m : cls.getDeclaredMethods()) {
                        if (m.isAnnotationPresent(meteordevelopment.orbit.EventHandler.class) && !Modifier.isStatic(m.getModifiers())) {
                            throw new IllegalStateException("Feature class '" + cls.getName() + "' declares a non-static @EventHandler method '" + m.getName() + "' — feature event handlers must be static when subscribing the class. Use a static method or have the registry subscribe an instance.");
                        }
                    }


                    for (Field f : cls.getDeclaredFields()) {
                        if (!SettingGeneric.class.isAssignableFrom(f.getType())) continue;
                        if (!java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                        f.setAccessible(true);
                        SettingGeneric setting = (SettingGeneric) f.get(null);
                        if (setting != null) {
                            // Previously settings used @SettingDescription; use the description provided
                            // by the Setting instance itself (last constructor argument) instead.
                            String descValue = setting.description();
                            // If description missing, derive one from the field name and
                            // convert to sentence case (keep acronyms uppercase)
                            if (descValue == null || descValue.isEmpty()) {
                                descValue = deriveDescriptionFromName(f.getName());
                            }
                            // override key: use field name as config key
                            setting.overrideKey(f.getName());
                            // Only override parent if the setting did not already specify one
                            String currentParent = setting.getParent();
                            if (currentParent == null || currentParent.isEmpty()) {
                                setting.overrideParent(feat.key());
                            }
                            info.settings.add(new SettingInfo(Utils.humanize(f.getName()), descValue, setting));
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

    // Split a CamelCase name into a human-friendly form while keeping consecutive
    // uppercase acronyms together (e.g., "RGBColor" -> "RGB Color").
    private static String splitCamelCasePreserveAcronyms(String name) {
        if (name == null || name.isEmpty()) return "";
        StringBuilder out = new StringBuilder();
        int n = name.length();
        for (int i = 0; i < n; i++) {
            char c = name.charAt(i);
            char prev = i > 0 ? name.charAt(i - 1) : 0;
            char next = i < n - 1 ? name.charAt(i + 1) : 0;

            if (i > 0) {
                if (Character.isLowerCase(prev) && Character.isUpperCase(c)) {
                    out.append(' ');
                } else if (Character.isUpperCase(prev) && Character.isUpperCase(c) && Character.isLowerCase(next)) {
                    out.append(' ');
                }
            }
            out.append(c);
        }
        return out.toString().trim();
    }

    // Convenience wrapper used by the registry: split the name and then convert to
    // sentence case (capitalize first letter) while preserving acronyms.
    private static String deriveDescriptionFromName(String name) {
        String split = splitCamelCasePreserveAcronyms(name);
        if (split == null || split.isEmpty()) return "";
        split = split.trim();
        return Character.toUpperCase(split.charAt(0)) + (split.length() > 1 ? split.substring(1) : "");
    }

    public static void reconcileFeatureSubscriptions() {
        for (FeatureInfo info : FEATURES) {
            if (info.featureInstance.isActive()) {
                try {
                    Main.LOGGER.info("Subscribing feature class {} to event", info.clazz.getName());
                    Main.eventBus.subscribe(info.clazz);
                } catch (Throwable t) {
                    Main.LOGGER.error("Failed to subscribe feature class {} during reconciliation", info.clazz.getName(), t);
                }
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
            name = featureInstance.name();
            description = featureInstance.description();
        }
    }

    public record SettingInfo(String name, String description, SettingGeneric settingInstance) {
    }
}
