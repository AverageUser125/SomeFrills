package com.somefrills.events.core

import com.somefrills.Main
import com.somefrills.events.FrillsEvent
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.function.Consumer

typealias EventPredicate = (event: FrillsEvent) -> Boolean

class EventListeners private constructor(val name: String) {

    private val listeners: MutableList<Listener> = mutableListOf()

    constructor(event: Class<*>) : this(
        (event.name.split(".").lastOrNull() ?: event.name).replace("$", "."),
    )

    fun removeListener(listener: Any) {
        listeners.removeIf { it.invoker == listener }
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun addListener(method: Method, instance: Any, options: EventHandle) {
        val name = buildListenerName(method)
        val eventConsumer = when (method.parameterCount) {
            0 -> createZeroParameterConsumer(method, instance)
            1 -> createSingleParameterConsumer(method, instance)
            else -> throw IllegalArgumentException(
                "Method ${method.name} must have either 0 or 1 parameters.",
            )
        }
        listeners.add(Listener(name, eventConsumer, options))
    }

    fun addListener(consumer: Consumer<Any>, name: String, options: EventHandle) {
        listeners.add(Listener(name, consumer, options))
    }

    private fun buildListenerName(method: Method): String {
        val paramTypesString = method.parameterTypes.joinTo(
            StringBuilder(),
            prefix = "(",
            postfix = ")",
            separator = ", ",
            transform = Class<*>::getTypeName,
        ).toString()

        return "${method.declaringClass.name}.${method.name}$paramTypesString"
    }

    private fun createZeroParameterConsumer(method: Method, instance: Any): (Any) -> Unit {
        val runnable = ReflectionUtils.createRunnableFromMethod(instance, method)
        return { _: Any -> runnable.run() }
    }

    private fun createSingleParameterConsumer(method: Method, instance: Any): (Any) -> Unit {
        val consumer = ReflectionUtils.createConsumerFromMethod(instance, method)
        return { event -> consumer.accept(event) }
    }
    fun getListeners(): List<Listener> = listeners

    class Listener(
        val name: String,
        val invoker: Consumer<Any>,
        val priority: Int,
        extraPredicates: List<EventPredicate> = listOf(),
    ) {

        constructor(
            name: String,
            invoke: Consumer<Any>,
            options: EventHandle,
            extraPredicates: List<EventPredicate> = listOf())
                : this(name, invoke, options.priority, extraPredicates)

        private var lastTick = -1
        private var cachedPredicateValue = false
        @Suppress("JoinDeclarationAndAssignment")
        private val predicates: List<EventPredicate>

        fun shouldInvoke(event: FrillsEvent): Boolean {
            if (lastTick != Main.totalTicks) {
                cachedPredicateValue = predicates.all { it(event) }
                lastTick = Main.totalTicks
            }
            return cachedPredicateValue && predicates.all { it(event) }
        }

        init {
            predicates = buildList {
                add { event -> !event.isCancelled }
                addAll(extraPredicates)
            }
        }
    }

}