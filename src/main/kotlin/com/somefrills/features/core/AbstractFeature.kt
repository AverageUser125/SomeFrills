package com.somefrills.features.core

import com.somefrills.Main.eventBus
import io.github.notenoughupdates.moulconfig.observer.Property

// Invariant: enabled = false && active = true cannot happen
abstract class AbstractFeature protected constructor(private val enabledProperty: Property<Boolean>) {
    private var active = false

    /**
     * Step 2 initialization.
     * Must be called once after construction.
     */
    fun initialize() {
        enabledProperty.addObserver { _: Boolean, n: Boolean ->
            if (n) {
                onEnable()
            } else {
                onDisable()
            }
            sync()
        }
        this.enabled = this.enabled
    }

    var enabled: Boolean
        get() = enabledProperty.get()
        set(enabled) {
            enabledProperty.set(enabled)
        }

    fun isActive(): Boolean {
        return active
    }

    open fun toggle() {
        this.enabled = !this.enabled
    }

    private fun setActive(value: Boolean) {
        if (!this.enabled && value) return

        if (value == active) return
        active = value

        if (value) {
            onActivate()
            eventBus.subscribe(this)
        } else {
            eventBus.unsubscribe(this)
            onDeactivate()
        }
    }

    fun sync() {
        if (!this.enabled) {
            setActive(false)
            return
        }

        setActive(evaluate())
    }

    protected abstract fun evaluate(): Boolean

    protected open fun onEnable() {
    }

    protected open fun onDisable() {
    }

    protected open fun onActivate() {
    }

    protected open fun onDeactivate() {
    }

    protected fun isEnabled(): Boolean {
        return enabled
    }
}