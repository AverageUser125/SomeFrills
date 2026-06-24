package com.somefrills.features.tweaks

import com.somefrills.config.FrillsMod
import com.somefrills.modules.FrillsFeature
import com.somefrills.features.core.PassiveFeature

@FrillsFeature
object ItemCountFix : PassiveFeature() {
    val config get() = FrillsMod.config.tweaks.itemCountFix

    override fun isActive(): Boolean {
        return config
    }
}