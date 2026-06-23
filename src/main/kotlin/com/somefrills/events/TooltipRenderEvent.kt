package com.somefrills.events

import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack


class TooltipRenderEvent(
    var lines: MutableList<Component?>,
    var stack: ItemStack?,
    var customData: CompoundTag?,
    var title: String?
) {
    fun addLine(line: Component?) {
        try {
            lines.add(line)
        } catch (ignored: UnsupportedOperationException) {
        }
    }

    class Before(stack: ItemStack?, title: String?) : Cancellable() {
        var stack: ItemStack?
        var title: String?

        init {
            this.isCancelled = false
            this.stack = stack
            this.title = title
        }
    }
}