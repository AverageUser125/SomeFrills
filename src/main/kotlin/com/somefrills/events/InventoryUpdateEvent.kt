package com.somefrills.events

import net.minecraft.item.ItemStack
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket

class InventoryUpdateEvent(
    var packet: ScreenHandlerSlotUpdateS2CPacket,
    @JvmField var stack: ItemStack,
    @JvmField var slotId: Int
)
