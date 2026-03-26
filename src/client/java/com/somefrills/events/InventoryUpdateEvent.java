package com.somefrills.events;

import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.item.ItemStack;

public class InventoryUpdateEvent {
    public ScreenHandlerSlotUpdateS2CPacket packet;
    public int slotId;
    public ItemStack stack;

    public InventoryUpdateEvent(ScreenHandlerSlotUpdateS2CPacket packet, ItemStack stack, int slotId) {
        this.packet = packet;
        this.stack = stack;
        this.slotId = slotId;
    }
}
