package com.somefrills.features.tweaks

import com.google.common.collect.Sets
import com.somefrills.Main.mc
import com.somefrills.config.FrillsConfig
import com.somefrills.features.core.Feature
import com.somefrills.features.core.FrillsFeature
import com.somefrills.misc.Utils
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import org.lwjgl.glfw.GLFW

@FrillsFeature
object MiddleClickOverride : Feature(FrillsConfig.tweaks.middleClickOverrideEnabled) {
    private val matchBlacklist = Sets.newHashSet<String?>(
        "Attribute Fusion",
        "Beacon",
        "Chest",
        "Large Chest",
        "Anvil",
        "Storage",
        "Drill Anvil",
        "Runic Pedestal",
        "Rune Removal",
        "Reforge Anvil",
        "Reforge Item",
        "Offer Pets",
        "Exp Sharing",
        "Convert to Dungeon Item",
        "Upgrade Item",
        "Salvage Items",
        Utils.format("A{}iphone", Utils.Symbols.bingo),
        "Fishing Rod Parts",
        "Stats Tuning",
        "Pet Sitter",
        "Transfer to Profile",
        "Attribute Transfer",
        "Hunting Box"
    )
    private val matchWhitelist = Sets.newHashSet<String?>(
        "Your Equipment and Stats",
        "Accessory Bag Thaumaturgy",
        "Community Shop"
    )
    private val containBlacklist = Sets.newHashSet<String?>(
        "Wardrobe",
        "Minion",
        "Abiphone",
        "The Hex",
        "Enchant Item",
        "Auction",
        "Cosmetic",
        "Trap",
        "Gemstone",
        "Heart of the",
        "Widgets"
    )
    private val containWhitelist = Sets.newHashSet<String?>(
        "Pets",
        "Bits Shop"
    )

    private fun isLeftClick(button: Int, actionType: SlotActionType): Boolean {
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT && actionType == SlotActionType.PICKUP
    }

    private fun isBlacklisted(title: String): Boolean {
        return matchBlacklist.contains(title) || containBlacklist.stream()
            .anyMatch { s: String? -> title.contains(s!!) }
    }

    private fun isWhitelisted(title: String): Boolean {
        return matchWhitelist.contains(title) || containWhitelist.stream()
            .anyMatch { s: String? -> title.contains(s!!) }
    }

    private fun isTransaction(stack: ItemStack?): Boolean {
        return Utils.getLoreLines(stack).stream()
            .anyMatch { line: String? -> line == "Cost" || line == "Sell Price" || line == "Bazaar Price" }
    }

    @JvmStatic
    fun shouldOverride(slot: Slot?, button: Int, actionType: SlotActionType): Boolean {
        if (!FrillsConfig.tweaks.middleClickOverrideEnabled.get()) return false
        val currentScreen = mc.currentScreen ?: return false
        if (slot == null) return false
        if (!isLeftClick(button, actionType)) return false

        val title: String = currentScreen.getTitle().string
        val stack = slot.stack

        if (stack.isEmpty) return false
        if (isBlacklisted(title)) return false
        if (!Utils.isInSkyblock()) return false

        return Utils.getSkyblockId(stack).isEmpty() || isWhitelisted(title) || isTransaction(stack)
    }
}
