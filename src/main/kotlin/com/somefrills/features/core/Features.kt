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
}