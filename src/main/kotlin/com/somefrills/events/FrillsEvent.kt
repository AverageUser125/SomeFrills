package com.somefrills.events

import com.somefrills.Main
import meteordevelopment.orbit.ICancellable

abstract class FrillsEvent protected constructor() {
    fun post() {
        Main.eventBus.post(this)
    }

    abstract class Cancellable : FrillsEvent(), ICancellable {
        private var cancelled: Boolean = false

        override fun isCancelled(): Boolean {
            return cancelled
        }

        override fun setCancelled(cancelled: Boolean) {
            this.cancelled = cancelled
        }
    }
}