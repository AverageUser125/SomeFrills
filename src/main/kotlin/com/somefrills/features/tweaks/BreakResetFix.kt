// TODO(Ravel): Failed to fully resolve file: null cannot be cast to non-null type com.intellij.psi.PsiClass
package com.somefrills.features.tweaks

import com.somefrills.Main.mc
import com.somefrills.config.FrillsMod

import com.somefrills.events.InventoryUpdateEvent
import com.somefrills.features.core.Feature
import com.somefrills.features.core.FrillsFeature
import com.somefrills.mixin.MultiPlayerGameModeAccessor
import meteordevelopment.orbit.EventHandler

@FrillsFeature
object BreakResetFix : Feature(FrillsMod.config.tweaks.breakResetFixEnabled) {
    @EventHandler
    fun onBreakReset(event: InventoryUpdateEvent) {
        if (mc.player == null || mc.gameMode == null) return
        if (event.slotId in 36..44 && mc.player?.getInventory()
                ?.selectedSlot == event.slotId - 36
        ) {
            (mc.gameMode as MultiPlayerGameModeAccessor).setStack(event.stack)
        } // manually update the variable once the server updates our held item, prevents the mismatch and thus fixes the break cancel
    }
}
