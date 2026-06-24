package com.somefrills.features.tweaks

import com.somefrills.config.FrillsMod
import com.somefrills.modules.FrillsFeature
import com.somefrills.features.core.PassiveFeature

@FrillsFeature
object MiddleClickFix : PassiveFeature(){
    val config get() = FrillsMod.config.tweaks.middleClickFixEnabled;

    override fun isActive(): Boolean {
        return config
    }
}