package com.somefrills.features.misc.glowmob.chestui

import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack


interface UIAddon {
    // Allows the addon to filter or slice the item list
    fun processItems(ui: ChestUI, items: MutableList<ItemStack>)

    // Allows the addon to place its own buttons (arrows, compass, etc.)
    fun drawDecoration(ui: ChestUI, inventory: Container)

    // Allows the addon to intercept clicks
    fun onClick(ui: ChestUI, stack: ItemStack, name: String, button: Int): Boolean
}