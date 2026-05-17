package com.somefrills.events

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Text

class TooltipRenderEvent(
    var lines: MutableList<Text>,
    val stack: ItemStack,
    val customData: NbtCompound,
    val title: String
) : FrillsEvent() {
    fun addLine(line: Text) {
        try {
            lines.add(line)
        } catch (_: UnsupportedOperationException) {
        }
    }
}
