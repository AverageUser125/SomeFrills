package com.somefrills.events

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Text

class TooltipRenderEvent(
    var lines: MutableList<Text>,
    var stack: ItemStack,
    var customData: NbtCompound,
    var title: String
) : FrillsEvent() {
    fun addLine(line: Text) {
        try {
            lines.add(line)
        } catch (_: UnsupportedOperationException) {
        }
    }
}
