package com.somefrills.features.core

import com.somefrills.events.AreaChangeEvent
import com.somefrills.misc.Area
import com.somefrills.misc.KeybindManager
import com.somefrills.misc.KeybindManager.register
import com.somefrills.misc.SkyblockData.area
import io.github.notenoughupdates.moulconfig.observer.Property

/**
 * A feature that combines area checking (like AreaFeature) with keybind toggling (like ToggleFeature).
 * Only becomes active when BOTH the player is in the correct area AND the keybind is toggled on.
 */
abstract class AreaToggleFeature(enabledProperty: Property<Boolean>, private val keybindProperty: Property<Int>) :
    AbstractFeature(enabledProperty) {
    private var keybindSub: KeybindManager.Subscription? = null
    private var keybindActive = false

    /**
     * Check if the current area is the correct one for this feature.
     */
    protected abstract fun checkArea(area: Area): Boolean

    /**
     * Both area check AND keybind toggle must be true for the feature to be active.
     */
    override fun evaluate(): Boolean {
        return keybindActive && checkArea(area)
    }

    override fun onEnable() {
        if (keybindSub != null) {
            keybindSub!!.unregister()
            keybindSub = null
        }
        keybindSub = register(keybindProperty) { this.toggleActive() }
        EventSubscriptions.register(this, AreaChangeEvent::class.java)
    }

    override fun onDisable() {
        if (keybindSub != null) {
            keybindSub!!.unregister()
            keybindSub = null
        }
        keybindActive = false
        EventSubscriptions.unregister(this)
    }

    protected fun toggleActive() {
        if (!isEnabled()) return
        keybindActive = !keybindActive
        sync()
    }
}

