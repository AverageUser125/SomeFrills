package com.somefrills.events;

import com.somefrills.misc.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;

public class EntityNamedEvent {
    public Entity entity;
    public Text name;
    public String namePlain;

    public EntityNamedEvent(Entity entity, Text name) {
        this.entity = entity;
        this.name = name;
        this.namePlain = Utils.toPlain(name);
    }
}
