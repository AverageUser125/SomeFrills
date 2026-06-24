package com.somefrills.events

import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.inventory.Slot
import com.somefrills.events.FrillsEvent.Cancellable

class SlotClickEvent(
    val slot: Slot?,
    val slotId: Int,
    val button: Int,
    val actionType: ContainerInput,
    val title: String,
    val handler: AbstractContainerMenu
) : FrillsEvent(), Cancellable
