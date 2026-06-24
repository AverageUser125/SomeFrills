package com.somefrills.events
import com.somefrills.events.FrillsEvent.Cancellable

class KeyUpEvent(
    val keyCode: Int
) : FrillsEvent(), Cancellable