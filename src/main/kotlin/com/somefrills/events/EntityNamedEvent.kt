package com.somefrills.events

import com.somefrills.utils.toPlain
import net.minecraft.entity.Entity
import net.minecraft.text.Text

class EntityNamedEvent(val entity: Entity, val name: Text) : FrillsEvent() {
    val namePlain get() = name.toPlain()
}
