package com.somefrills.events

import net.minecraft.entity.Entity
import net.minecraft.util.hit.EntityHitResult

class InteractEntityEvent(var entity: Entity, var entityHitResult: EntityHitResult) : Cancellable()
