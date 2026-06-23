package com.somefrills.events

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.Entity.RemovalReason

class EntityRemovedEvent(@JvmField var entity: Entity, var reason: RemovalReason) : FrillsEvent()