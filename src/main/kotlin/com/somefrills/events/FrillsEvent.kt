package com.somefrills.events

import com.somefrills.Main
import meteordevelopment.orbit.ICancellable

abstract class FrillsEvent protected constructor() {
    open fun post(): Boolean {
        Main.eventBus.post(this)
        return false
    }
}