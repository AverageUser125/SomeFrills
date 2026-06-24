package com.somefrills.events

import net.minecraft.world.entity.Entity;
import com.somefrills.events.FrillsEvent.Cancellable

class AttackEntityEvent(val entity: Entity) : FrillsEvent(), Cancellable
