package com.somefrills.events

import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType

class SlotClickEvent(
    var slot: Slot,
    var slotId: Int,
    var button: Int,
    var actionType: SlotActionType,
    var title: String,
    var handler: ScreenHandler
) : Cancellable()
