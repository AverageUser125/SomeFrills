package com.somefrills.events

import net.minecraft.world.entity.Entity

class EntityUpdatedEvent(@JvmField val entity: Entity) : FrillsEvent()
