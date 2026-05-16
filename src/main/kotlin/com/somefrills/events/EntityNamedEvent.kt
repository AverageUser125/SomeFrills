package com.somefrills.events

import com.somefrills.misc.Utils
import net.minecraft.entity.Entity
import net.minecraft.text.Text

class EntityNamedEvent(var entity: Entity, var name: Text) : FrillsEvent() {
    var namePlain: String = Utils.toPlain(name)
}
