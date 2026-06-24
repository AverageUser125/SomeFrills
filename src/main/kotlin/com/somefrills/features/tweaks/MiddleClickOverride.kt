package com.somefrills.features.tweaks

import com.google.common.collect.Sets
import com.somefrills.Main.mc
import com.somefrills.config.FrillsMod

import com.somefrills.features.core.Feature
import com.somefrills.modules.FrillsFeature
import com.somefrills.utils.SkyblockUtils
import com.somefrills.utils.Symbols
import com.somefrills.utils.TextUtils
import com.somefrills.utils.getLoreLines
import com.somefrills.utils.skyblockId
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import org.lwjgl.glfw.GLFW

@FrillsFeature
object MiddleClickOverride : Feature(FrillsMod.config.tweaks.middleClickOverrideEnabled) {
    private val matchBlacklist = Sets.newHashSet<String>(
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
        TextUtils.format("A{}iphone", Symbols.bingo),
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

    private fun isLeftClick(button: Int, actionType: ContainerInput): Boolean {
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT && actionType == ContainerInput.PICKUP
    }

    private fun isBlacklisted(title: String): Boolean {
        return matchBlacklist.contains(title) || containBlacklist.stream()
            .anyMatch { s: String? -> title.contains(s!!) }
    }

    private fun isWhitelisted(title: String): Boolean {
        return matchWhitelist.contains(title) || containWhitelist.stream()
            .anyMatch { s: String? -> title.contains(s!!) }
    }

    private fun isTransaction(stack: ItemStack): Boolean {
        return stack.getLoreLines().stream()
            .anyMatch { line: String? -> line == "Cost" || line == "Sell Price" || line == "Bazaar Price" }
    }

    @JvmStatic
    fun shouldOverride(slot: Slot?, button: Int, actionType: ContainerInput): Boolean {
        if (!FrillsMod.config.tweaks.middleClickOverrideEnabled.get()) return false
        val currentScreen = mc.screen ?: return false
        if (slot == null) return false
        if (!isLeftClick(button, actionType)) return false

        val title: String = currentScreen.getTitle().string
        val stack = slot.item

        if (stack.isEmpty) return false
        if (isBlacklisted(title)) return false
        if (!SkyblockUtils.isInSkyblock()) return false
        if (stack.skyblockId?.isEmpty() == true) return false

        return isWhitelisted(title) || isTransaction(stack)
    }
}