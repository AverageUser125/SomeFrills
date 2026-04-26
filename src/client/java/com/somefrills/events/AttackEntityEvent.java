package com.somefrills.events;

import net.minecraft.entity.Entity;

public class AttackEntityEvent extends Cancellable {
    public Entity entity;

    public AttackEntityEvent(Entity entity) {
        this.entity = entity;
    }
}
