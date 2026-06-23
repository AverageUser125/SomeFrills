package com.somefrills.features.misc.glowmob.chestui

import com.somefrills.features.misc.glowmob.MatchInfo
import com.somefrills.features.misc.glowmob.MatchInfo.GearFlag
import com.somefrills.utils.getCustomData
import com.somefrills.utils.setCustomData
import com.somefrills.utils.setCustomName
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.DyedItemColor

class ArmorSelectionMenu(previousScreen: ChestUI, info: MatchInfo) : ChestUI("Select Armor Type", previousScreen) {
    private val gear: MutableSet<GearFlag> = info.gear

    init {
        rebuild()
    }

    // ========================
    // Helpers
    // ========================
    private fun `is`(flag: GearFlag): Boolean {
        return gear.contains(flag)
    }

    private val isNaked: Boolean
        get() = gear.contains(GearFlag.NAKED)

    private val isNone: Boolean
        get() = gear.isEmpty()

    private fun toggleArmor(flag: GearFlag) {
        if (gear.contains(flag)) {
            gear.remove(flag)
            return
        }

        // switching to armor removes naked
        gear.remove(GearFlag.NAKED)

        gear.add(flag)
    }

    private fun toggleNaked() {
        if (gear.contains(GearFlag.NAKED)) {
            gear.remove(GearFlag.NAKED)
            return
        }

        gear.clear()
        gear.add(GearFlag.NAKED)
    }

    // ========================
    // UI build
    // ========================
    override fun build() {
        allItems.clear()

        val naked = this.isNaked
        val none = this.isNone

        addItem(
            "Chestplate", GearFlag.CHEST, Items.LEATHER_CHESTPLATE,
            !naked && `is`(GearFlag.CHEST)
        )

        addItem(
            "Leggings", GearFlag.LEGS, Items.LEATHER_LEGGINGS,
            !naked && `is`(GearFlag.LEGS)
        )

        addItem(
            "Boots", GearFlag.FEET, Items.LEATHER_BOOTS,
            !naked && `is`(GearFlag.FEET)
        )

        addItem(
            "Helmet", GearFlag.HEAD, Items.LEATHER_HELMET,
            !naked && `is`(GearFlag.HEAD)
        )

        addItem("Naked", GearFlag.NAKED, Items.BARRIER, naked)

        addItem("None", null, Items.PAPER, none)
    }

    private fun addItem(name: String?, flag: GearFlag?, itemType: Item, enabled: Boolean) {
        val item = ItemStack(itemType)

        val color = if (enabled) 0x00FF00 else 0xFF5555

        val status = if (enabled) " [ON]" else " [OFF]"

        item.setCustomName(
            Style.EMPTY.withColor(TextColor.fromRgb(color)),
            name + status
        )

        val colorComponent = DyedItemColor(color)
        item[DataComponents.DYED_COLOR] = colorComponent
        item.remove(DataComponents.ATTRIBUTE_MODIFIERS)

        if (flag != null) {
            val data = CompoundTag()
            data.putString("Flag", flag.name)
            item.setCustomData(data)
        }
        addItem(item)
    }

    // ========================
    // Click handling
    // ========================
    override fun onItemClick(stack: ItemStack?, button: Int) {
        val customData = stack?.getCustomData() ?: run {
            gear.clear()
            rebuild()
            return
        }
        val flagName = customData.get("Flag")!!.asString().orElse(null)
        if (flagName == null) {
            gear.clear()
            rebuild()
            return
        }

        val flag = GearFlag.valueOf(flagName)
        if (flag == GearFlag.NAKED) {
            toggleNaked()
        } else {
            toggleArmor(flag)
        }

        rebuild()
    }
}