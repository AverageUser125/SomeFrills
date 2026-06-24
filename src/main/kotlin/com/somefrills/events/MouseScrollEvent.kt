package com.somefrills.events
import com.somefrills.events.FrillsEvent.Cancellable

class MouseScrollEvent(@JvmField val value: Double) : FrillsEvent(), Cancellable
