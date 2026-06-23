package com.somefrills.events

import net.minecraft.world.entity.Entity;

class AttackEntityEvent(val entity: Entity) : Cancellable()
