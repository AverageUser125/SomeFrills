package com.somefrills.features.tweaks

import com.google.common.collect.Sets
import com.somefrills.config.FrillsMod

import com.somefrills.events.PlaceBlockEvent
import com.somefrills.features.core.Feature
import com.somefrills.features.core.FrillsFeature
import com.somefrills.utils.hasRightClickAbility
import com.somefrills.utils.skyblockId
import meteordevelopment.orbit.EventHandler
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemPlacementContext

@FrillsFeature
object NoAbilityPlace : Feature(FrillsMod.config.tweaks.noAbilityPlaceEnabled) {
    private val abilityWhitelist = Sets.newHashSet<String?>(
        "ABINGOPHONE",
        "SUPERBOOM_TNT",
        "INFINITE_SUPERBOOM_TNT",
        "ARROW_SWAPPER",
        "PUMPKIN_LAUNCHER",
        "SNOW_CANNON",
        "SNOW_BLASTER",
        "SNOW_HOWITZER"
    )

    @EventHandler
    fun onPlaceBlock(event: PlaceBlockEvent): Boolean {
        return hasAbility(event.context)
    }

    @JvmStatic
    fun hasAbility(context: ItemPlacementContext?): Boolean {
        if (!isActive()) return false
        if (context == null) return false
        val stack = context.stack
        val id = stack.skyblockId ?: return false
        if (!id.isEmpty() && (abilityWhitelist.contains(id) || id.startsWith("ABIPHONE"))) {
            return true
        }
        return stack.item is BlockItem && stack.hasRightClickAbility
    }
}
