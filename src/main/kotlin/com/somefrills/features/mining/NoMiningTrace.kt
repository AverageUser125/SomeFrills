package com.somefrills.features.mining

import com.somefrills.config.FrillsMod
import com.somefrills.features.core.PassiveFeature

object NoMiningTrace : PassiveFeature() {
    val config get() = FrillsMod.config.mining.noMiningTrace

    override fun isActive(): Boolean {
        return config.enabled.get()
    }
}