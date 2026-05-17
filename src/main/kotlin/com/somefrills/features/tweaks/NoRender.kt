package com.somefrills.features.tweaks

import com.somefrills.features.core.FrillsFeature
import com.somefrills.features.core.PassiveFeature

@FrillsFeature
object NoRender: PassiveFeature() {
    @JvmStatic
    val config get() = com.somefrills.config.FrillsMod.config.tweaks.noRender
}