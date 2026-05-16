package com.somefrills.events

import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.slot.Slot

class SlotUpdateEvent(
    var packet: ScreenHandlerSlotUpdateS2CPacket,
    var screen: GenericContainerScreen,
    var handler: GenericContainerScreenHandler,
    var slotId: Int
) {
    var inventory: Inventory = handler.inventory
    var slot: Slot? =
        if ((this.slotId >= 0) and (this.slotId < this.handler.slots.size)) this.handler.getSlot(this.slotId) else null
    var stack: ItemStack = this.inventory.getStack(this.slotId)
    var title: String? = screen.getTitle().string
    var isFinal: Boolean = packet.slot == handler.slots.last().id
    var isInventory: Boolean = this.stack == ItemStack.EMPTY
}
