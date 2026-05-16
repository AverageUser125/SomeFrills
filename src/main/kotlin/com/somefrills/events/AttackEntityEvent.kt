package com.somefrills.events

import net.minecraft.entity.Entity

class AttackEntityEvent(var entity: Entity) : Cancellable()
