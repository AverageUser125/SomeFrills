package com.somefrills.utils

import com.somefrills.events.WorldRenderEvent
import com.somefrills.Main.mc
import com.somefrills.misc.RenderColor
import com.somefrills.mixininterface.EntityRendering
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityPose
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.entity.SimpleEntityLookup
import java.util.function.Predicate
import kotlin.collections.ArrayList

object EntityUtils {
    fun getPlayers(): List<PlayerEntity> {
        return getStreamEntities(PlayerEntity::class.java).toList()
    }

    fun getEntities(): List<Entity> {
        return if (mc.world != null) {
            val lookup = mc.world!!.entityManager.getLookup() as SimpleEntityLookup<Entity>
            ArrayList(lookup.index.idToEntity.values)
        } else {
            ArrayList()
        }
    }

    fun <T, R : T> filterAndCast(clazz: Class<R>): (T) -> Sequence<R> {
        return { t -> if (clazz.isInstance(t)) sequenceOf(clazz.cast(t)) else emptySequence() }
    }

    fun <T> getStreamEntities(clazz: Class<T>): Sequence<T> {
        return getEntities().asSequence().flatMap(filterAndCast(clazz))
    }

    fun horizontalDistance(from: Vec3d, to: Vec3d): Float {
        val x = (from.x - to.x).toFloat()
        val z = (from.z - to.z).toFloat()
        return MathHelper.sqrt(x * x + z * z)
    }

    fun findNametagOwner(armorStandEntity: Entity, otherEntities: List<Entity>): Entity? {
        var entity: Entity? = null
        var lowestDist = 2.0f
        val maxY = armorStandEntity.entityPos.y
        for (ent in otherEntities) {
            val dist = horizontalDistance(ent.entityPos, armorStandEntity.entityPos)
            if (ent !is ArmorStandEntity && ent.entityPos.y < maxY && dist < lowestDist) {
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

fun Entity.getLerpedBox(event: WorldRenderEvent): Box {
    return getLerpedBox(event.tickCounter.getTickProgress(true))
}

fun Entity.getLerpedBox(tickProgress: Float): Box {
    return getDimensions(EntityPose.STANDING).getBoxAt(getLerpedPos(tickProgress))
}

fun Entity.horizontalDistance(to: Entity): Float {
    return EntityUtils.horizontalDistance(entityPos, to.entityPos)
}

fun Entity.horizontalDistance(to: Vec3d): Float {
    return EntityUtils.horizontalDistance(entityPos, to)
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

fun Entity.getOtherEntitiesAround(box: Box, filter: Predicate<Entity>): List<Entity> {
    val entities = ArrayList<Entity>()
    for (ent in EntityUtils.getEntities()) {
        if (ent != this && filter.test(ent) && ent.boundingBox.intersects(box)) {
            entities.add(ent)
        }
    }
    return entities
}

fun Entity.getOtherEntitiesAround(distX: Double, distY: Double, distZ: Double, filter: Predicate<Entity>): List<Entity> {
    return getOtherEntitiesAround(Box.of(entityPos, distX, distY, distZ), filter)
}

fun Entity.getOtherEntitiesAround(dist: Double, filter: Predicate<Entity>): List<Entity> {
    return getOtherEntitiesAround(Box.of(entityPos, dist, dist, dist), filter)
}

// ========== LivingEntity Extension Functions ==========

fun LivingEntity.getEquippedArmor(): List<ItemStack> {
    return listOf(
        getEquippedStack(EquipmentSlot.HEAD),
        getEquippedStack(EquipmentSlot.CHEST),
        getEquippedStack(EquipmentSlot.LEGS),
        getEquippedStack(EquipmentSlot.FEET)
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
    return if (this is net.minecraft.entity.player.PlayerEntity) {
        !PlayerUtils.isRealPlayer(this)
    } else {
        this is LivingEntity
    }
}
