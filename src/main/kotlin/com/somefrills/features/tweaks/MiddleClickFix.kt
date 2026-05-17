package com.somefrills.features.tweaks

import com.somefrills.config.FrillsMod
import com.somefrills.features.core.PassiveFeature

object MiddleClickFix : PassiveFeature(){
    val config get() = FrillsMod.config.tweaks.middleClickFixEnabled;

    override fun isActive(): Boolean {
        return config
    }
}