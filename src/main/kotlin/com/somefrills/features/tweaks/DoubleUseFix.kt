package com.somefrills.features.tweaks

import com.somefrills.Main.mc
import com.somefrills.config.FrillsMod

import com.somefrills.events.InteractBlockEvent
import com.somefrills.events.InteractItemEvent
import com.somefrills.features.core.Feature
import com.somefrills.features.core.FrillsFeature
import com.somefrills.utils.EntityUtils
import com.somefrills.utils.PlayerUtils
import com.somefrills.utils.getRightClickAbility
import meteordevelopment.orbit.EventHandler
import net.minecraft.item.Items
import net.minecraft.util.Hand
import net.minecraft.util.hit.HitResult

@FrillsFeature
object DoubleUseFix : Feature(FrillsMod.config.tweaks.doubleUseFixEnabled) {
    @EventHandler
    private fun onUseItem(event: InteractItemEvent) {
        if (mc.crosshairTarget != null && mc.crosshairTarget?.type == HitResult.Type.BLOCK && disableType == Type.Dagger) {
            event.cancel()
        }
    }

    @EventHandler
    private fun onUseBlock(event: InteractBlockEvent) {
        if (disableType != Type.Rod) return

        mc.interactionManager?.interactItem(mc.player, Hand.MAIN_HAND)
        mc.player?.swingHand(Hand.MAIN_HAND)
        event.cancel()
    }

    private enum class Type {
        Dagger,
        Rod,
        None
    }

    private val disableType: Type
        get() {
            val held = PlayerUtils.getHeldItem()
            if (held.item == Items.FISHING_ROD) {
                return Type.Rod
            }
            if (held.getRightClickAbility().contains("Attunement")) {
                return Type.Dagger
            }
            return Type.None
        }
}