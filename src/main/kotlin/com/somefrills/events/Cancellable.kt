package com.somefrills.events

import com.somefrills.Main
import meteordevelopment.orbit.ICancellable

abstract class Cancellable : FrillsEvent(), ICancellable {
    override fun post(): Boolean {
        Main.eventBus.post(this)
        return isCancelled
    }

    private var cancelled: Boolean = false

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(cancelled: Boolean) {
        this.cancelled = cancelled
    }
}