package com.somefrills.events

import net.minecraft.entity.Entity
import net.minecraft.entity.Entity.RemovalReason

class EntityRemovedEvent(@JvmField var entity: Entity, var reason: RemovalReason) : FrillsEvent()