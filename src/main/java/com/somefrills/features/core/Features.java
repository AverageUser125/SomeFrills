package com.somefrills.features.core;

public final class Features {

    private static final AbstractFeature[] INSTANCES = FeaturesRegistry.INSTANCES;
    private static final ClassValue<AbstractFeature> CLASS_TO_INSTANCE = FeaturesRegistry.CLASS_TO_INSTANCE;

    private static volatile boolean initialized = false;

    public static boolean isInitialized() {
        return initialized;
    }

    public static void init() {
        FeaturesRegistry.init();
        for (AbstractFeature feature : INSTANCES) {
            feature.initialize();
        }
        initialized = true;
    }

    public static <T extends AbstractFeature> T get(Class<T> clazz) {
        AbstractFeature feature = CLASS_TO_INSTANCE.get(clazz);
        if (feature == null) return null;

        @SuppressWarnings("unchecked")
        T instance = (T) feature;

        return instance;
    }

    public static <T extends AbstractFeature> boolean isActive(Class<T> clazz) {
        var instance = get(clazz);
        if (instance == null) return false;
        return instance.isActive();
    }
}