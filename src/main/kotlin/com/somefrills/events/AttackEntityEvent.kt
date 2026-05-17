package com.somefrills.events

import net.minecraft.entity.Entity

class AttackEntityEvent(val entity: Entity) : Cancellable()
