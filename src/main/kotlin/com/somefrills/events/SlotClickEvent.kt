package com.somefrills.events

import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType

class SlotClickEvent(
    val slot: Slot?,
    val slotId: Int,
    val button: Int,
    val actionType: SlotActionType,
    val title: String,
    val handler: ScreenHandler
) : Cancellable()
