package com.somefrills.features.tweaks

import com.somefrills.Main.mc
import com.somefrills.config.FrillsMod

import com.somefrills.events.InteractBlockEvent
import com.somefrills.events.InteractItemEvent
import com.somefrills.features.core.Feature
import com.somefrills.features.core.FrillsFeature
import com.somefrills.utils.PlayerUtils
import com.somefrills.utils.getRightClickAbility
import meteordevelopment.orbit.EventHandler
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.Items
import net.minecraft.world.phys.HitResult

@FrillsFeature
object DoubleUseFix : Feature(FrillsMod.config.tweaks.doubleUseFixEnabled) {
    @EventHandler
    private fun onUseItem(event: InteractItemEvent) {
        val hitResult = mc.hitResult ?: return
        if (hitResult.type == HitResult.Type.BLOCK && disableType == Type.Dagger) {
            event.cancel();
        }
    }

    @EventHandler
    private fun onUseBlock(event: InteractBlockEvent) {
        if (disableType != Type.Rod) return
        val player = mc.player ?: return
        mc.gameMode?.useItem(player, InteractionHand.MAIN_HAND)
        mc.player?.swing(InteractionHand.MAIN_HAND)
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