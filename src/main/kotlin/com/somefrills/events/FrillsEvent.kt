package com.somefrills.events

import com.somefrills.events.core.FrillsEvents

abstract class FrillsEvent protected constructor() {
    var isCancelled: Boolean = false
        private set

    fun post(): FrillsEvent {
        FrillsEvents.getEventHandler(javaClass).post(this)
        return this
    }

    interface Cancellable {
        fun cancel() {
            val event = this as FrillsEvent
            event.isCancelled = true
        }
    }
}