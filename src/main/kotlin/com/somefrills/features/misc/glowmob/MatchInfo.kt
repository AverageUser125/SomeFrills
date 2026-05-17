package com.somefrills.features.misc.glowmob

import com.google.gson.*
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.somefrills.misc.Area
import com.somefrills.misc.SortedList
import com.somefrills.misc.Utils
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.decoration.ArmorStandEntity
import java.io.Serial
import java.util.*
import java.util.function.Predicate
import kotlin.math.hypot

class MatchInfo {
    @JvmField
    var type: SortedList<String>
    @JvmField
    var name: String
    @JvmField
    var area: Area?
    @JvmField
    var gear: MutableSet<GearFlag>
    @JvmField
    var maxHp: Int

    constructor(info: MatchInfo) {
        this.type = SortedList(info.type)
        this.name = info.name
        this.area = info.area
        this.gear = EnumSet.copyOf(info.gear)
        this.maxHp = info.maxHp
    }

    constructor(type: MutableList<String>, name: String, area: Area?, gear: MutableSet<GearFlag>, maxHp: Int) {
        this.type = SortedList<String>(type)
        this.name = name
        this.area = area
        this.gear = gear
        this.maxHp = maxHp
    }

    constructor() {
        this.type = SortedList()
        this.name = ""
        this.area = null
        this.gear = EnumSet.noneOf(GearFlag::class.java)
        this.maxHp = 0
    }

    val isEmpty: Boolean
        get() = type.isEmpty() && name.trim { it <= ' ' }.isEmpty() && area == null && gear.isEmpty() && maxHp <= 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MatchInfo) return false
        return type == other.type
                && name == other.name
                && ((area == null && other.area == null) || (area != null && area == other.area))
                && gear == other.gear
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (if (area != null) area.hashCode() else 0)
        result = 31 * result + gear.hashCode()
        return result
    }

    override fun toString(): String {
        return serialize()!!
    }

    fun clear() {
        this.type = SortedList()
        this.name = ""
        this.area = null
        this.gear.clear()
    }

    fun compile(): Predicate<LivingEntity> {
        var predicate = Predicate { e: LivingEntity -> true }
        if (area != null) {
            predicate = predicate.and(AreaPredicate(area))
        }
        if (!type.isEmpty()) {
            predicate = predicate.and(MultiTypePredicate(type))
        }
        if (!name.trim { it <= ' ' }.isEmpty()) {
            predicate = predicate.and(NamePredicate(name))
        }
        if (!gear.isEmpty()) {
            if (gear.contains(GearFlag.NAKED)) {
                predicate = predicate.and(NakedPredicate())
            } else {
                predicate = predicate.and(GearPredicate(gear))
            }
        }
        if (maxHp > 0) {
            predicate = predicate.and(MaxHpPredicate(maxHp))
        }
        return predicate
    }

    fun toJson(): JsonObject {
        val obj = JsonObject()

        if (!type.isEmpty()) {
            val types = JsonArray()
            for (t in type) {
                types.add(t)
            }
            obj.add("type", types)
        }

        if (!name.isEmpty()) {
            obj.addProperty("name", name)
        }

        if (area != null) {
            obj.addProperty("area", area!!.displayName)
        }

        if (!gear.isEmpty()) {
            val gears = JsonArray()
            for (g in gear) {
                gears.add(g.name)
            }
            obj.add("gear", gears)
        }

        if (maxHp > 0) {
            obj.addProperty("maxHp", maxHp)
        }

        return obj
    }

    fun serialize(): String? {
        return toJson().toString()
    }

    enum class GearFlag {
        NAKED,
        CHEST,
        LEGS,
        FEET,
        HEAD
    }

    class MatchInfoTypeAdapter : TypeAdapter<MatchInfo>() {
        private val gson = Gson()

        override fun write(out: JsonWriter, value: MatchInfo) {
            gson.toJson(value.toJson(), out)
        }

        override fun read(`in`: JsonReader): MatchInfo {
            val obj = JsonParser.parseReader(`in`).getAsJsonObject()
            try {
                return fromJson(obj)
            } catch (e: MatcherParseException) {
                throw JsonParseException("Failed to parse MatchInfo: " + e.message, e)
            }
        }
    }

    class AreaPredicate(private val area: Area?) : Predicate<LivingEntity?> {
        override fun test(entity: LivingEntity?): Boolean {
            return Utils.isInArea(area)
        }
    }

    private class NamePredicate(name: String) : Predicate<LivingEntity> {
        override fun test(entity: LivingEntity): Boolean {
            return hasNamedArmorStandAbove(entity)
        }

        fun hasNamedArmorStandAbove(entity: LivingEntity): Boolean {
            val eX = entity.x
            val eY = entity.y
            val eZ = entity.z

            for (nearby in getNearbyEntities(entity)) {
                if (nearby !is ArmorStandEntity) {
                    continue
                }

                val asY = nearby.y
                if (asY < eY || asY - eY >= VERTICAL_RANGE) {
                    continue
                }

                val dx = nearby.x - eX
                val dz = nearby.z - eZ
                val horizontalDist = hypot(dx, dz)

                if (horizontalDist <= HORIZONTAL_RADIUS) {
                    val asName = Utils.toPlain(nearby.displayName).lowercase(Locale.getDefault())
                    if (asName.contains(this.name)) {
                        return true
                    }
                }
            }

            return false
        }

        val name: String = name.lowercase(Locale.getDefault())

        companion object {
            private fun getNearbyEntities(entity: LivingEntity): MutableList<LivingEntity> {
                val world = entity.entityWorld ?: return mutableListOf<LivingEntity>()

                val box = entity.boundingBox
                    .expand(HORIZONTAL_RADIUS * 2, VERTICAL_RANGE, HORIZONTAL_RADIUS * 2)

                return world.getEntitiesByClass(
                    ArmorStandEntity::class.java,
                    box,
                    Predicate { e: ArmorStandEntity -> true })
                    .stream()
                    .map { e: ArmorStandEntity -> e as LivingEntity }
                    .toList()
            }
        }
    }

    class GearPredicate(private val requiredGear: MutableSet<GearFlag>) : Predicate<LivingEntity> {
        override fun test(entity: LivingEntity): Boolean {
            return requiredGear.contains(GearFlag.CHEST) && !entity.getEquippedStack(EquipmentSlot.CHEST)
                .isEmpty() || requiredGear.contains(GearFlag.LEGS) && !entity.getEquippedStack(
                EquipmentSlot.LEGS
            ).isEmpty() || requiredGear.contains(GearFlag.FEET) && !entity.getEquippedStack(
                EquipmentSlot.FEET
            ).isEmpty() || requiredGear.contains(GearFlag.HEAD) && !entity.getEquippedStack(
                EquipmentSlot.HEAD
            ).isEmpty()
        }
    }

    class NakedPredicate : Predicate<LivingEntity> {
        override fun test(entity: LivingEntity): Boolean {
            // FIXME: hurtTime is not good, as it means the mob may flicker, but it should still work
            // Without this checks, mobs that are dying or recently spawned will be considered naked, which is not ideal
            if (entity.isDead() || entity.age <= 2 || entity.deathTime > 0 || entity.hurtTime > 0) {
                return false
            }

            return Utils.isNaked(entity)
        }
    }

    class MaxHpPredicate(private val maxHp: Int) : Predicate<LivingEntity> {
        override fun test(entity: LivingEntity): Boolean {
            return entity.getMaxHealth() == maxHp.toFloat()
        }
    }

    class MatcherParseException(message: String?) : Exception(message) {
        companion object {
            @Serial
            private const val serialVersionUID = 1L
        }
    }

    class MultiTypePredicate(private val entityTypes: MutableList<String?>) : Predicate<LivingEntity> {
        override fun test(entity: LivingEntity): Boolean {
            val entityTypeStr = entity.getType().toString().lowercase(Locale.getDefault())
            return entityTypes.stream().anyMatch { s: String? -> Companion.specializedEquals(entityTypeStr, s!!) }
        }

        companion object {
            private val PREFIX_LENGTH = "entity.minecraft.".length

            fun specializedEquals(entityTypeStr: String, entityType: String): Boolean {
                if (entityTypeStr.length - PREFIX_LENGTH != entityType.length) {
                    return false
                }
                for (i in 0..<entityType.length) {
                    val c1 = entityTypeStr.get(i + PREFIX_LENGTH)
                    val c2 = entityType.get(i)
                    if (c1 != c2) {
                        return false
                    }
                }
                return true
            }
        }
    }

    companion object {
        private const val HORIZONTAL_RADIUS = 0.25
        private const val VERTICAL_RANGE = 4.0

        @Throws(MatcherParseException::class)
        fun fromString(str: String): MatchInfo {
            if (str.trim { it <= ' ' }.isEmpty()) {
                throw MatcherParseException("Empty matcher expression")
            }
            try {
                val obj = JsonParser.parseString(str).getAsJsonObject()
                return fromJson(obj)
            } catch (e: JsonSyntaxException) {
                throw MatcherParseException("Invalid JSON format: " + e.message)
            } catch (e: IllegalStateException) {
                throw MatcherParseException("Invalid JSON format: " + e.message)
            }
        }

        @JvmStatic
        @Throws(MatcherParseException::class)
        fun fromJson(obj: JsonObject): MatchInfo {
            val info = MatchInfo()

            if (obj.has("type")) {
                info.type = SortedList()
                for (el in obj.getAsJsonArray("type")) {
                    info.type.add(el.getAsString())
                }
            }

            if (obj.has("name")) {
                info.name = obj.get("name").getAsString()
            }

            if (obj.has("area")) {
                info.area = Area.fromString(obj.get("area").getAsString())
            }

            if (obj.has("gear")) {
                info.gear = EnumSet.noneOf(GearFlag::class.java)

                for (el in obj.getAsJsonArray("gear")) {
                    try {
                        info.gear.add(GearFlag.valueOf(el.getAsString().uppercase(Locale.getDefault())))
                    } catch (e: IllegalArgumentException) {
                        throw MatcherParseException("Unknown gear: " + el.getAsString())
                    }
                }
            }

            if (obj.has("maxHp")) {
                try {
                    info.maxHp = obj.get("maxHp").getAsInt()
                } catch (e: NumberFormatException) {
                    throw MatcherParseException("Invalid maxHp value")
                }
            }

            return info
        }
    }
}
