package com.somefrills.events

import net.minecraft.entity.Entity
import net.minecraft.util.hit.EntityHitResult

class InteractEntityEvent(val entity: Entity, val entityHitResult: EntityHitResult) : Cancellable()
