package com.somefrills.events

import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.world.item.ItemStack

class InventoryUpdateEvent(
    val packet: ClientboundContainerSetSlotPacket,
    @JvmField val stack: ItemStack,
    @JvmField val slotId: Int
) : FrillsEvent()