package com.somefrills.features.core

import com.somefrills.events.FrillsEvent
import com.somefrills.events.core.EventListeners.Listener
import com.somefrills.events.core.FrillsEvents
import java.util.function.Consumer

object EventSubscriptions {

    data class Subscription(
        val eventClass: Class<Any>,
        val listener: Listener
    )
    private val listeners: MutableMap<Any, Subscription> = HashMap()

    /**
     * Register callback for event type.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : FrillsEvent> register(owner: Any, eventClass: Class<T>, action: Consumer<T>) {
        unregister(owner)

        val listener = Listener("onEvent", action as Consumer<Any>, 0)

        listeners[owner] = Subscription(eventClass as Class<Any>, listener)
        FrillsEvents.registerListener(eventClass, listener)
    }

    /**
     * Convenience overload for no event usage.
     */
    fun <T : FrillsEvent> register(owner: Any, eventClass: Class<T>, action: Runnable) {
        register(owner, eventClass) { e: T -> action.run() }
    }

    fun <T : FrillsEvent> register(feature: AbstractFeature, eventClass: Class<T>) {
        register(feature, eventClass) { e: T -> feature.sync() }
    }

    @Suppress("UNCHECKED_CAST")
    fun unregister(owner: Any) {
        val listener = listeners.remove(owner)
        if (listener != null) {
            FrillsEvents.unregisterListener(listener.eventClass as Class<out FrillsEvent>, listener.listener)
        }
    }

    fun clear() {
        val owners = listeners.keys.toList()
        for (owner in owners) {
            unregister(owner)
        }
        listeners.clear()
    }
}