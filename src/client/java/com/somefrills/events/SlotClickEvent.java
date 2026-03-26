package com.somefrills.events;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

public class SlotClickEvent extends Cancellable {
    public Slot slot;
    public int slotId;
    public int button;
    public ClickType actionType;
    public String title;
    public AbstractContainerMenu handler;

    public SlotClickEvent(Slot slot, int slotId, int button, ClickType actionType, String title, AbstractContainerMenu handler) {
        this.setCancelled(false);
        this.slot = slot;
        this.slotId = slotId;
        this.button = button;
        this.actionType = actionType;
        this.title = title;
        this.handler = handler;
    }
}
