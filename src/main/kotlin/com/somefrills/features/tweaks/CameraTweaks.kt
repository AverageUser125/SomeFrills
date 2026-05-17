package com.somefrills.features.tweaks

import com.somefrills.config.FrillsConfig
import com.somefrills.config.tweaks.TweaksCategory.CameraTweaksConfig
import com.somefrills.features.core.FrillsFeature
import com.somefrills.features.core.PassiveFeature

@FrillsFeature
class CameraTweaks : PassiveFeature() {
    private val config get() = FrillsConfig.tweaks.cameraTweaks

    fun clip(): Boolean {
        return config.clip
    }
}
