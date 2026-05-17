package com.somefrills.features.core

object Features {

    private val registry = FeaturesRegistry
    private var initialized = false

    @JvmStatic
    fun isInitialized(): Boolean {
        return initialized
    }

    @JvmStatic
    fun init() {
        registry.init()

        for (f in registry.INSTANCES.filterNotNull()) {
            f.initialize()
        }

        initialized = true
    }

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T : AbstractFeature> get(clazz: Class<T>): T {
        val f = registry.CLASS_TO_INSTANCE.get(clazz)
            ?: error("No feature for $clazz")
        return f as T
    }

    @JvmStatic
    fun <T : AbstractFeature> isActive(clazz: Class<T>): Boolean {
        return get(clazz).isActive()
    }

    @JvmStatic
    fun all(): List<AbstractFeature> {
        return registry.INSTANCES.filterNotNull()
    }
}