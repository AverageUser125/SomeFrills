package com.somefrills.misc

import com.somefrills.events.EntityRemovedEvent
import com.somefrills.events.EntityUpdatedEvent
import com.somefrills.events.ServerJoinEvent
import meteordevelopment.orbit.EventHandler
import meteordevelopment.orbit.EventPriority
import net.minecraft.entity.Entity

/**
 * An object for temporarily storing any relevant entity handles, such as armor stands with custom names.
 */
class EntityCache {
    private val entities = ConcurrentHashSet<Entity>()

    init {
        instances.add(this)
    }

    fun has(ent: Entity): Boolean {
        return this.entities.contains(ent)
    }

    fun empty(): Boolean {
        return this.entities.isEmpty()
    }

    fun size(): Int {
        return this.entities.size
    }

    /**
     * Adds an entity handle to the object. Does nothing if the entity is already on the list.
     */
    fun add(ent: Entity) {
        this.entities.add(ent)
    }

    /**
     * Removes an entity handle from the object. Does nothing if the entity is not on the list.
     */
    fun remove(ent: Entity) {
        this.entities.remove(ent)
    }

    fun clear() {
        this.entities.clear()
    }

    fun get(): ConcurrentHashSet<Entity> {
        return this.entities
    }

    val first: Entity?
        get() = this.entities.stream().findFirst().orElse(null)

    companion object {
        private val instances: MutableList<EntityCache> = ArrayList()

        @EventHandler(priority = EventPriority.LOW)
        private fun onRemoved(event: EntityRemovedEvent) {
            for (instance in instances) {
                instance.remove(event.entity)
            }
        }

        @EventHandler(priority = EventPriority.LOW)
        private fun onUpdated(event: EntityUpdatedEvent) {
            if (event.entity.isRemoved) {
                for (instance in instances) {
                    instance.remove(event.entity)
                }
            }
        }

        @EventHandler(priority = EventPriority.LOW)
        private fun onJoin(event: ServerJoinEvent) {
            for (instance in instances) {
                instance.clear()
            }
        }
    }
}