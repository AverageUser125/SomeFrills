package com.somefrills.features.misc.glowmob.chestui

import com.somefrills.utils.setCustomName
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Style
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class CloseAddon : UIAddon {
    // Default slot is middle-bottom (Index 49 for a 9x6 chest)
    var closeSlot: Int = ChestUI.INV_SIZE - 9 + 4

    override fun processItems(ui: ChestUI, items: MutableList<ItemStack>) {
        // This addon doesn't filter or modify the content list
    }

    override fun drawDecoration(ui: ChestUI, inventory: Container) {
        if (ui.previousScreen == null) return  // Don't show close button if there's no previous screen to go back to

        val closeButton = ItemStack(Items.BARRIER)
        val barrierStyle = Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false)
        closeButton.setCustomName(barrierStyle, "Close")

        inventory.setItem(closeSlot, closeButton)
    }

    override fun onClick(ui: ChestUI, stack: ItemStack, name: String, button: Int): Boolean {
        if (name == "Close") {
            ui.onClose()
            return true
        }
        return false
    }
}