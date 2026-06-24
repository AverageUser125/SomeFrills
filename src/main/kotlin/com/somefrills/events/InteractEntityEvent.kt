package com.somefrills.events

import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.EntityHitResult
import com.somefrills.events.FrillsEvent.Cancellable

class InteractEntityEvent(val entity: Entity, val entityHitResult: EntityHitResult) : FrillsEvent(), Cancellable
