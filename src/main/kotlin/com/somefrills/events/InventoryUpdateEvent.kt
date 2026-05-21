package com.somefrills.events

import net.minecraft.item.ItemStack
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket

class InventoryUpdateEvent(
    val packet: ScreenHandlerSlotUpdateS2CPacket,
    @JvmField val stack: ItemStack,
    @JvmField val slotId: Int
) : FrillsEvent()