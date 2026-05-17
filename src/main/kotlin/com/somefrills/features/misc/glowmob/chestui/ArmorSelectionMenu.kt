package com.somefrills.features.misc.glowmob.chestui

import com.somefrills.features.misc.glowmob.MatchInfo
import com.somefrills.features.misc.glowmob.MatchInfo.GearFlag
import com.somefrills.misc.Utils
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.AttributeModifiersComponent
import net.minecraft.component.type.DyedColorComponent
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Style
import net.minecraft.text.TextColor

class ArmorSelectionMenu(previousScreen: ChestUI?, info: MatchInfo) : ChestUI("Select Armor Type", previousScreen) {
    private val gear: MutableSet<GearFlag>

    init {
        this.gear = info.gear
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

        Utils.setCustomName(
            item,
            Style.EMPTY.withColor(TextColor.fromRgb(color)),
            name + status
        )

        val colorComponent = DyedColorComponent(color)
        item.set<DyedColorComponent?>(DataComponentTypes.DYED_COLOR, colorComponent)
        item.remove<AttributeModifiersComponent?>(DataComponentTypes.ATTRIBUTE_MODIFIERS)

        if (flag != null) {
            val data = NbtCompound()
            data.putString("Flag", flag.name)
            Utils.setCustomData(item, data)
        }
        addItem(item)
    }

    // ========================
    // Click handling
    // ========================
    override fun onItemClick(stack: ItemStack?, button: Int) {
        val customData = Utils.getCustomData(stack)
        if (customData == null) {
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