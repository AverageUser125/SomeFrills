package com.somefrills.features.core

import com.somefrills.misc.KeybindManager
import com.somefrills.misc.KeybindManager.register
import io.github.notenoughupdates.moulconfig.observer.Property

abstract class ToggleFeature(
    enabledProperty: Property<Boolean>,
    private val keybindProperty: Property<Int>
) : AbstractFeature(enabledProperty) {
    private var sub: KeybindManager.Subscription? = null
    private var keybindActive = false

    override fun evaluate(): Boolean {
        return keybindActive
    }

    override fun toggle() {
        if (!isEnabled()) return

        keybindActive = !keybindActive
        sync()
    }

    override fun onEnable() {
        if (sub != null) {
            sub!!.unregister()
            sub = null
        }
        sub = register(keybindProperty, Runnable { this.toggle() })
    }

    override fun onDisable() {
        if (sub != null) {
            sub!!.unregister()
            sub = null
        }
        keybindActive = false
    }
}