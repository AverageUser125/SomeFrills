package com.somefrills.events.core

import com.somefrills.events.FrillsEvent
import com.somefrills.utils.CollectionUtils.removeIfKey
import java.lang.reflect.Method
import kotlin.collections.get
import kotlin.jvm.java

object FrillsEvents {

    private val listeners: MutableMap<Class<out FrillsEvent>, EventListeners> = mutableMapOf()
    private val handlers: MutableMap<Class<out FrillsEvent>, EventHandler<out FrillsEvent>> = mutableMapOf()

    fun register(instance: Any) {
        instance.javaClass.declaredMethods.forEach {
            registerMethod(it, instance)
        }
    }

    fun unregister(instance: Any) = instance.javaClass.declaredMethods.forEach(::unregisterMethod)

    @Suppress("UNCHECKED_CAST")
    fun <T : FrillsEvent> getEventHandler(event: Class<T>): EventHandler<T> = handlers.getOrPut(event) {
        EventHandler(
            event,
            getEventClasses(event).mapNotNull { listeners[it] }.flatMap(EventListeners::getListeners),
        )
    } as EventHandler<T>

    private fun registerMethod(method: Method, instance: Any) {
        val (options, eventTypes) = getEventData(method) ?: return
        eventTypes.forEach { eventType ->
            listeners.getOrPut(eventType) { EventListeners(eventType) }
                .addListener(method, instance, options)
        }
    }

    fun registerListener(event: Class<out FrillsEvent>, listener: EventListeners.Listener) {
        listeners.getOrPut(event) { EventListeners(event) }.addListener(listener)
    }

    fun unregisterListener(event: Class<out FrillsEvent>, listener: EventListeners.Listener) {
        listeners[event]?.removeListener(listener)
    }

    @JvmStatic
    val eventPrimaryFunctionNames: Map<String, Class<out FrillsEvent>> =
        GeneratedEventPrimaryFunctionNames.map

    @Suppress("UNCHECKED_CAST")
    private fun getEventData(method: Method): Pair<EventHandle, List<Class<out FrillsEvent>>>? {
        val options = method.getAnnotation(EventHandle::class.java) ?: return null
        when (method.parameterCount) {
            1 -> {
                val eventType = method.parameterTypes.first()
                require(FrillsEvent::class.java.isAssignableFrom(eventType)) {
                    "Method ${method.name} parameter must be a subclass of FrillsEvent."
                }
                return options to listOf(eventType as Class<out FrillsEvent>)
            }

            0 -> {
                val primaryFunctionEventType = eventPrimaryFunctionNames[method.name]
                require(primaryFunctionEventType != null) {
                    "Method ${method.name} has no parameters and is not a primary function of any event. " +
                            "Please specify the event type(s) in @HandleEvent."
                }
                return options to listOf(primaryFunctionEventType)
            }
        }
        return null
    }

    private fun unregisterMethod(method: Method) {
        val (_, eventTypes) = getEventData(method) ?: return
        eventTypes.forEach { event ->
            unregisterHandler(event)
            listeners.values.forEach { it.removeListener(method) }
        }
    }

    private fun unregisterHandler(clazz: Class<out FrillsEvent>) {
        this.handlers.removeIfKey { it.isAssignableFrom(clazz) }
    }

    /**
     * Returns a list of all super classes and the class itself up to [FrillsEvent].
     */
    private fun getEventClasses(clazz: Class<*>): List<Class<*>> {
        val classes = mutableListOf<Class<*>>()
        classes.add(clazz)

        var current = clazz
        @Suppress("LoopWithTooManyJumpStatements")
        while (current.superclass != null) {
            val superClass = current.superclass
            if (superClass == FrillsEvent::class.java) break
            classes.add(superClass)
            current = superClass
        }
        return classes
    }
}