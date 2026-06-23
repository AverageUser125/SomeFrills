package com.somefrills.events

import com.somefrills.utils.toPlain
import net.minecraft.world.entity.Entity
import net.minecraft.network.chat.Component

class EntityNamedEvent(val entity: Entity, val name: Component) : FrillsEvent() {
    val namePlain get() = name.toPlain()
}
