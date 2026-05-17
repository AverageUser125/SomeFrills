package com.somefrills.features.tweaks

import com.somefrills.config.FrillsMod

import com.somefrills.features.core.FrillsFeature
import com.somefrills.features.core.PassiveFeature

@FrillsFeature
object CameraTweaks : PassiveFeature() {
    private val config get() = FrillsMod.config.tweaks.cameraTweaks

    fun clip(): Boolean {
        return config.clip
    }
}
