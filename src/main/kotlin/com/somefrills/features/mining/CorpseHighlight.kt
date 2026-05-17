package com.somefrills.features.mining

import at.hannibal2.skyhanni.features.mining.glacitemineshaft.MineshaftWaypoint
import at.hannibal2.skyhanni.features.mining.glacitemineshaft.MineshaftWaypoints.waypoints
import com.somefrills.config.FrillsConfig
import com.somefrills.config.mining.MiningCategory.CorpseHighlightConfig
import com.somefrills.events.TickEventPre
import com.somefrills.features.core.AreaFeature
import com.somefrills.features.core.FrillsFeature
import com.somefrills.misc.Area
import com.somefrills.misc.RenderColor.Companion.fromChroma
import com.somefrills.misc.Utils
import io.github.notenoughupdates.moulconfig.ChromaColour
import meteordevelopment.orbit.EventHandler
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.item.ItemStack
import java.util.function.Predicate

@FrillsFeature
class CorpseHighlight : AreaFeature(FrillsConfig.mining.corpseHighlight.enabled) {
    private val config: CorpseHighlightConfig
        get() = FrillsConfig.mining.corpseHighlight

    private fun getCorpseColor(type: CorpseType): ChromaColour? {
        return when (type) {
            CorpseType.Lapis -> config.lapisColor
            CorpseType.Tungsten -> config.mineralColor
            CorpseType.Umber -> config.yogColor
            CorpseType.Vanguard -> config.vanguardColor
            else -> null
        }
    }


    @EventHandler
    private fun onTick(event: TickEventPre?) {
        val stands = Utils.getStreamEntities(ArmorStandEntity::class.java)
            .filter { stand: ArmorStandEntity ->
                if (stand.isInvisible) return@filter false
                if (!stand.shouldShowArms()) return@filter false
                !stand.shouldShowBasePlate()
            }.toList()

        for (stand in stands) {
            val colour = getCorpseColor(getCorpseType(stand)) ?: continue
            val color = fromChroma(colour)
            Utils.setGlowing(stand, true, color)
        }
    }

    override fun checkArea(area: Area): Boolean {
        return area == Area.MINESHAFT
    }

    enum class CorpseType {
        Lapis,
        Tungsten,
        Umber,
        Vanguard,
        None
    }


    companion object {
        private fun getCorpseType(ent: ArmorStandEntity): CorpseType {
            val armor = Utils.getEntityArmor(ent)
            if (armor.isEmpty()) return CorpseType.None

            val helmet: ItemStack = armor[0]
            if (helmet.isEmpty) return CorpseType.None
            return when (Utils.toPlain(helmet.name)) {
                "Lapis Armor Helmet" -> CorpseType.Lapis
                "Mineral Helmet" -> CorpseType.Tungsten
                "Yog Helmet" -> CorpseType.Umber
                "Vanguard Helmet" -> CorpseType.Vanguard
                else -> CorpseType.None
            }
        }


        private fun shareAllWaypoints(filter: Predicate<MineshaftWaypoint>): MutableList<String> {
            val sb = ArrayList<String>()
            val waypoints = waypoints
            for (waypoint in waypoints) {
                if (!filter.test(waypoint)) continue
                val location = waypoint.location.toChatFormat()
                val type = waypoint.waypointType.displayText

                val message = String.format("%s | (%s)", location, type)
                sb.add(message)
                waypoint.shared = true
            }
            return sb
        }

        @JvmStatic
        fun shareAllWaypointsForce(): MutableList<String> {
            return shareAllWaypoints { waypoint: MineshaftWaypoint -> waypoint.isCorpse }
        }

        @JvmStatic
        fun shareAllWaypoints(): MutableList<String> {
            return shareAllWaypoints { waypoint: MineshaftWaypoint -> !waypoint.shared && waypoint.isCorpse }
        }
    }
}