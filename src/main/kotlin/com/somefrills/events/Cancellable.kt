package com.somefrills.events

import meteordevelopment.orbit.ICancellable

abstract class Cancellable : FrillsEvent(), ICancellable {
    private var cancelled: Boolean = false

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(cancelled: Boolean) {
        this.cancelled = cancelled
    }
}