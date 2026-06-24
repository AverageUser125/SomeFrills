package com.somefrills.features.mining

import com.somefrills.config.FrillsMod
import com.somefrills.modules.FrillsFeature
import com.somefrills.features.core.PassiveFeature

@FrillsFeature
object NoMiningTrace : PassiveFeature() {
    val config get() = FrillsMod.config.mining.noMiningTrace

    override fun isActive(): Boolean {
        return config.enabled.get()
    }
}