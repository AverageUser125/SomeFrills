package com.somefrills.misc

import com.somefrills.Main
import com.somefrills.Main.mc
import com.somefrills.mixin.BaseObservableAccessor
import io.github.notenoughupdates.moulconfig.observer.Observer
import io.github.notenoughupdates.moulconfig.observer.Property

object KeybindManager {
    private val keybinds: MutableList<Keybind> = ArrayList<Keybind>()

    @JvmStatic
    fun onKeyPressed(key: Int) {
        if (mc.screen != null) return

        for (keybind in ArrayList<Keybind>(keybinds)) {
            if (keybind.key == key) {
                keybind.trigger()
            }
        }
    }

    private fun register(keybind: Keybind): Subscription {
        keybinds.add(keybind)
        return Subscription { keybinds.remove(keybind) }
    }

    @JvmStatic
    fun register(key: Int, action: Runnable): Subscription {
        return register(Keybind(key, action))
    }

    @JvmStatic
    fun register(property: Property<Int>, action: Runnable): Subscription {
        val initial = Keybind(property.get()!!, action)
        // Why use array? because we need to mutate the current subscription inside the observer, and Java's lambda requires captured variables to be effectively final.
        val current: Array<Subscription> = arrayOf(register(initial))

        @Suppress("SENSELESS_COMPARISON")
        val observer: Observer<Int?> = Observer<Int?> { _, newValue ->
            val old = current[0]
            current[0] = register(Keybind(newValue!!, action))
            if (old != null) old.unregister()
        }
        property.addObserver(observer)

        return Subscription {
            (property as BaseObservableAccessor<*>).getObservers().remove(observer)
            current[0].unregister()
        }
    }

    fun interface Subscription {
        fun unregister()
    }

    @JvmRecord
    private data class Keybind(val key: Int, val callback: Runnable) {
        fun trigger() {
            callback.run()
        }
    }
}