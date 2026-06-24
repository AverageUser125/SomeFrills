package com.somefrills.events.core

import com.somefrills.Main
import com.somefrills.events.FrillsEvent
import com.somefrills.utils.TextUtils

class EventHandler<T : FrillsEvent> private constructor(
    val name: String,
    private val listeners: List<EventListeners.Listener>,
) {

    constructor(event: Class<T>, listeners: List<EventListeners.Listener>) : this(
        (event.name.split(".").lastOrNull() ?: event.name).replace("$", "."),
        listeners.sortedBy { it.priority }.toList(),
    )

    fun post(event: T, onError: ((Throwable) -> Unit)? = null): Boolean {
        if (this.listeners.isEmpty()) return false

        for (listener in listeners) {
            if (!listener.shouldInvoke(event)) continue
            try {
                listener.invoker.accept(event)
            } catch (throwable: Throwable) {
                val errorName = throwable::class.simpleName ?: "error"
                val aOrAn = TextUtils.optionalAn(errorName)
                val message = "Caught $aOrAn $errorName in ${listener.name} at $name: ${throwable.message}"
                Main.LOGGER.error(message, throwable)
                onError?.invoke(throwable)
            }
            break
        }
        return event.isCancelled
    }
}