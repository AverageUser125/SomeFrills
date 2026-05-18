package com.somefrills.features.misc.glowmob.chestui

import com.somefrills.utils.setCustomName
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Style
import net.minecraft.util.Formatting

class CloseAddon : UIAddon {
    // Default slot is middle-bottom (Index 49 for a 9x6 chest)
    var closeSlot: Int = ChestUI.INV_SIZE - 9 + 4

    override fun processItems(ui: ChestUI, items: MutableList<ItemStack>) {
        // This addon doesn't filter or modify the content list
    }

    override fun drawDecoration(ui: ChestUI, inventory: Inventory) {
        if (ui.previousScreen == null) return  // Don't show close button if there's no previous screen to go back to

        val closeButton = ItemStack(Items.BARRIER)
        val barrierStyle = Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)
        closeButton.setCustomName(barrierStyle, "Close")

        inventory.setStack(closeSlot, closeButton)
    }

    override fun onClick(ui: ChestUI, stack: ItemStack, name: String, button: Int): Boolean {
        if (name == "Close") {
            ui.close()
            return true
        }
        return false
    }
}