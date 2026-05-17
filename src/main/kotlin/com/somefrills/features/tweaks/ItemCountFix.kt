package com.somefrills.features.tweaks

import com.somefrills.config.FrillsMod
import com.somefrills.features.core.PassiveFeature

object ItemCountFix : PassiveFeature() {
    val config get() = FrillsMod.config.tweaks.itemCountFix

    override fun isActive(): Boolean {
        return config
    }
}