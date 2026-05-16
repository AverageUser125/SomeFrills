package com.somefrills.events

import net.minecraft.entity.Entity

class EntityUpdatedEvent(@JvmField var entity: Entity) : FrillsEvent()
