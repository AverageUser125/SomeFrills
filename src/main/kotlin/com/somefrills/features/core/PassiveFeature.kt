package com.somefrills.features.core

import io.github.notenoughupdates.moulconfig.observer.Property

open class PassiveFeature protected constructor() : AbstractFeature(Property.of<Boolean>(false)) {
    override fun evaluate(): Boolean {
        return false
    }

    override fun onActivate() {
    }

    override fun onDeactivate() {
    }

    override fun onEnable() {
    }

    override fun onDisable() {
    }
}
