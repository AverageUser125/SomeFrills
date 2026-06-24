package com.somefrills.events

import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import com.somefrills.events.FrillsEvent.Cancellable

class TooltipRenderEvent(
    var lines: MutableList<Component>,
    var stack: ItemStack,
    var customData: CompoundTag?,
    var title: String?
) : FrillsEvent(){
    fun addLine(line: Component) {
        try {
            lines.add(line)
        } catch (ignored: UnsupportedOperationException) {
        }
    }

    class Before(var stack: ItemStack, var title: String) : FrillsEvent(), Cancellable {
    }
}