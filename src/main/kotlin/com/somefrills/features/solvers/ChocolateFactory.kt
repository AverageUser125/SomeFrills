package com.somefrills.features.solvers

import com.somefrills.config.FrillsConfig
import com.somefrills.config.solvers.SolverCategory.ChocolateFactoryConfig
import com.somefrills.events.ScreenRenderEvent
import com.somefrills.features.core.Feature
import com.somefrills.features.core.FrillsFeature
import com.somefrills.misc.Utils
import meteordevelopment.orbit.EventHandler
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity

@FrillsFeature
class ChocolateFactory : Feature(FrillsConfig.solvers.chocolateFactorySolver.enabled) {
    private val config get() = FrillsConfig.solvers.chocolateFactorySolver
    private var lastClaimTime: Long = 0

    @EventHandler
    fun onHudTick(event: ScreenRenderEvent) {
        val client = MinecraftClient.getInstance()
        if (client == null || client.player == null) return

        // Check if auto claim is enabled
        if (!config.claimStray) return

        val player: ClientPlayerEntity = client.player!!
        val handler = player.currentScreenHandler ?: return

        var title = ""
        if (client.currentScreen != null) {
            val txt = client.currentScreen!!.getTitle()
            if (txt != null) title = txt.getString()
        }

        // Check if we're in the Chocolate Factory menu
        if (title != CHOCOLATE_FACTORY_TITLE) return


        // Scan for claimable items
        for (slot in handler.slots) {
            val stack = slot.getStack()
            if (stack == null || stack.isEmpty()) continue

            val displayName = stack.getName().getString()

            // Check if this is a claimable item (CLICK ME! or Golden Rabbit)
            if (displayName.contains("CLICK ME!")) {
                // Check click delay
                if (System.currentTimeMillis() - lastClaimTime < config.claimDelay) return
                Utils.clickSlot(slot.getIndex())
                lastClaimTime = System.currentTimeMillis()
                return  // Only click one per tick
            }

            // Check if this is a claimable item (CLICK ME! or Golden Rabbit)
            if (displayName.contains("Golden Rabbit")) {
                // Check click delay
                if (System.currentTimeMillis() - lastClaimTime < config.claimDelay * 100L) return
                Utils.clickSlot(slot.getIndex())
                lastClaimTime = System.currentTimeMillis()
                return  // Only click one per tick
            }
        }
    }

    companion object {
        private const val CHOCOLATE_FACTORY_TITLE = "Chocolate Factory"
    }
}