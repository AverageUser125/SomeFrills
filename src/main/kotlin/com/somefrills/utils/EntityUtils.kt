package com.somefrills.utils

import com.somefrills.Main.mc
import com.somefrills.events.WorldRenderEvent
import com.somefrills.misc.RenderColor
import com.somefrills.mixininterface.EntityRendering
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.entity.LevelEntityGetterAdapter
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import java.util.function.Predicate


object EntityUtils {
    fun getPlayers(): List<Player> {
        return getStreamEntities(Player::class.java).toList()
    }

    fun getEntities(): MutableList<Entity> {
        val level = mc.level ?: return ArrayList<Entity>()
        val lookup = level.entityStorage.getEntityGetter() as LevelEntityGetterAdapter<Entity>
        return ArrayList(lookup.visibleEntities.byId.values)
    }

    fun <T, R : T> filterAndCast(clazz: Class<R>): (T) -> Sequence<R> {
        return { t -> if (clazz.isInstance(t)) sequenceOf(clazz.cast(t)) else emptySequence() }
    }

    fun <T> getStreamEntities(clazz: Class<T>): Sequence<T> {
        return getEntities().asSequence().flatMap(filterAndCast(clazz))
    }

    fun horizontalDistance(from: Vec3, to: Vec3): Float {
        val x = (from.x - to.x).toFloat()
        val z = (from.z - to.z).toFloat()
        return Mth.sqrt(x * x + z * z)
    }

    fun findNametagOwner(armorStandEntity: Entity, otherEntities: List<Entity>): Entity? {
        var entity: Entity? = null
        var lowestDist = 2.0f
        val maxY = armorStandEntity.position().y
        for (ent in otherEntities) {
            val dist = horizontalDistance(ent.position(), armorStandEntity.position())
            if (ent !is ArmorStand && ent.position().y < maxY && dist < lowestDist) {
                entity = ent
                lowestDist = dist
            }
        }
        return entity
    }

    @JvmStatic
    fun isGlowing(ent: Entity): Boolean {
        return ent.isGlowing
    }
}

// ========== Entity Extension Functions ==========

fun Entity.getLerpedBox(event: WorldRenderEvent): AABB {
    return getLerpedBox(event.tickCounter.gameTimeDeltaTicks)
}

fun Entity.getLerpedBox(tickProgress: Float): AABB {
    return getDimensions(Pose.STANDING).makeBoundingBox(getPosition(tickProgress));
}

fun Entity.horizontalDistance(to: Entity): Float {
    return EntityUtils.horizontalDistance(position(), to.position())
}

fun Entity.horizontalDistance(to: Vec3): Float {
    return EntityUtils.horizontalDistance(position(), to)
}

fun Entity.setGlowing(shouldGlow: Boolean, color: RenderColor) {
    (this as EntityRendering).`somefrills$setGlowingColored`(shouldGlow, color)
}

private fun LivingEntity.setGlowing(glowing: Boolean, color: RenderColor) {
    (this as EntityRendering).`somefrills$setGlowingColored`(glowing, color)
}

val Entity.isGlowing: Boolean
    get() = (this as EntityRendering).`somefrills$getGlowing`()

val Entity.plainName: String
    get() = this.name.toPlain()

fun Entity.findNametagOwners(otherEntities: List<Entity>): Entity? {
    return EntityUtils.findNametagOwner(this, otherEntities)
}

fun Entity.getOtherEntitiesAround(
    box: AABB,
    filter: Predicate<Entity>
): List<Entity> {
    val entities = ArrayList<Entity>()

    for (ent in EntityUtils.getEntities()) {
        if (ent !== this &&
            filter.test(ent) &&
            ent.boundingBox.intersects(box)
        ) {
            entities.add(ent)
        }
    }

    return entities
}

fun Entity.getOtherEntitiesAround(
    distX: Double,
    distY: Double,
    distZ: Double,
    filter: Predicate<Entity>
): List<Entity> {
    return getOtherEntitiesAround(
        AABB.ofSize(position(), distX, distY, distZ),
        filter
    )
}

fun Entity.getOtherEntitiesAround(
    dist: Double,
    filter: Predicate<Entity>
): List<Entity> {
    return getOtherEntitiesAround(
        AABB.ofSize(position(), dist, dist, dist),
        filter
    )
}

// ========== LivingEntity Extension Functions ==========

fun LivingEntity.getEquippedArmor(): List<ItemStack> {
    return listOf(
        getItemBySlot(EquipmentSlot.HEAD),
        getItemBySlot(EquipmentSlot.CHEST),
        getItemBySlot(EquipmentSlot.LEGS),
        getItemBySlot(EquipmentSlot.FEET)
    )
}

val LivingEntity.isNaked: Boolean
    get() = getEquippedArmor().all { it.isEmpty }

fun LivingEntity.isBaseHealth(health: Float): Boolean {
    val current = this.health
    val difference = current - health
    return current >= health && (current % health == 0f || (current - difference) % health == 0f)
}

// ========== Generic Entity Checking ==========

fun Entity.isMob(): Boolean {
    return if (this is Player) {
        !PlayerUtils.isRealPlayer(this)
    } else {
        this is LivingEntity
    }
}
