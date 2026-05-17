package com.somefrills.features.core

import com.somefrills.events.AreaChangeEvent
import com.somefrills.misc.Area
import com.somefrills.misc.SkyblockData.area
import io.github.notenoughupdates.moulconfig.observer.Property

abstract class AreaFeature(enabledProperty: Property<Boolean>) : AbstractFeature(enabledProperty) {
    protected abstract fun checkArea(area: Area): Boolean

    override fun evaluate(): Boolean {
        return checkArea(area)
    }

    override fun onEnable() {
        EventSubscriptions.register<AreaChangeEvent>(this, AreaChangeEvent::class.java)
    }

    override fun onDisable() {
        EventSubscriptions.unregister(this)
    }
}