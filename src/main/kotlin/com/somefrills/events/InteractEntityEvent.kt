package com.somefrills.events

import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.EntityHitResult

class InteractEntityEvent(val entity: Entity, val entityHitResult: EntityHitResult) : Cancellable()
