package com.somefrills.features.core

import com.somefrills.Main
import meteordevelopment.orbit.listeners.IListener
import java.util.function.Consumer

object EventSubscriptions {
    private val listeners: MutableMap<Any, IListener> = HashMap()

    /**
     * Register callback for event type.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> register(owner: Any, eventClass: Class<T>, action: Consumer<T>) {
        unregister(owner)

        val listener: IListener = object : IListener {
            override fun call(event: Any) {
                action.accept(event as T)
            }

            override fun getTarget(): Class<*> {
                return eventClass
            }

            override fun getPriority(): Int {
                return 0
            }

            @Deprecated("")
            override fun isStatic(): Boolean {
                return false
            }
        }

        listeners[owner] = listener
        Main.eventBus.subscribe(listener)
    }

    /**
     * Convenience overload for no event usage.
     */
    fun <T> register(owner: Any, eventClass: Class<T>, action: Runnable) {
        register(owner, eventClass) { e: T -> action.run() }
    }

    fun <T> register(feature: AbstractFeature, eventClass: Class<T>) {
        register(feature, eventClass) { e: T -> feature.sync() }
    }

    fun unregister(owner: Any) {
        val listener = listeners.remove(owner)
        if (listener != null) {
            Main.eventBus.unsubscribe(listener)
        }
    }

    fun clear() {
        for (listener in listeners.values) {
            Main.eventBus.unsubscribe(listener)
        }
        listeners.clear()
    }
}