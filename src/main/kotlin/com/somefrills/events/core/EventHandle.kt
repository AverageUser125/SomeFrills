package com.somefrills.events.core


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class EventHandle(
/**
 * The priority of when the event will be called, lower priority will be called first, see the companion object.
 */
val priority: Int = 0,
)