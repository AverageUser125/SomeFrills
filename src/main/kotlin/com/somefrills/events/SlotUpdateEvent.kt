package com.somefrills.events

import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.slot.Slot

class SlotUpdateEvent(
    val packet: ScreenHandlerSlotUpdateS2CPacket,
    val screen: GenericContainerScreen,
    val handler: GenericContainerScreenHandler,
    val slotId: Int
) {
    val inventory: Inventory get() = handler.inventory
    val slot: Slot? =
        if ((this.slotId >= 0) and (this.slotId < this.handler.slots.size)) this.handler.getSlot(this.slotId) else null
    val stack: ItemStack get() = this.inventory.getStack(this.slotId)
    val title: String get() = screen.getTitle().string
    val isFinal: Boolean get() = packet.slot == handler.slots.last().id
    val isInventory: Boolean get() = this.stack == ItemStack.EMPTY
}
