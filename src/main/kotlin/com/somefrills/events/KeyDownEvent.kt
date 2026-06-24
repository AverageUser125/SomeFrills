package com.somefrills.events
import com.somefrills.events.FrillsEvent.Cancellable

class KeyDownEvent(
    val keyCode: Int
) : FrillsEvent(), Cancellable