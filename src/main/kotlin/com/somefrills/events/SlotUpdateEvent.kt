package com.somefrills.events

import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.world.Container
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack


class SlotUpdateEvent(
    val packet: ClientboundContainerSetSlotPacket,
    val screen: ContainerScreen,
    val handler: ChestMenu,
    val inventory: Container,
    val slot: Slot?,
    val stack: ItemStack,
) {
    val title: String get() = screen.getTitle().string
    val isInventory: Boolean get() = this.stack == ItemStack.EMPTY
}
