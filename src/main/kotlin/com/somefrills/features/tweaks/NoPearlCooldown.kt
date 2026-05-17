package com.somefrills.features.tweaks

import com.somefrills.config.FrillsMod
import com.somefrills.features.core.PassiveFeature

object NoPearlCooldown : PassiveFeature(){
    val config get() = FrillsMod.config.tweaks.noAbilityPlaceEnabled


    override fun isActive(): Boolean {
        return NoPearlCooldown.config.get()
    }
}