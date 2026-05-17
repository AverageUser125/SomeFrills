package com.somefrills.features.tweaks

import com.somefrills.config.FrillsMod
import com.somefrills.features.core.FrillsFeature
import com.somefrills.features.core.PassiveFeature

@FrillsFeature
object NoPearlCooldown : PassiveFeature(){
    val config get() = FrillsMod.config.tweaks.noAbilityPlaceEnabled

    override fun isActive(): Boolean {
        return NoPearlCooldown.config.get()
    }
}