package com.somefrills.features.core

import io.github.notenoughupdates.moulconfig.observer.Property

abstract class Feature(enabledProperty: Property<Boolean>) : AbstractFeature(enabledProperty) {
    override fun evaluate(): Boolean {
        return true
    }

    override fun onEnable() {
    }

    override fun onDisable() {
    }
}