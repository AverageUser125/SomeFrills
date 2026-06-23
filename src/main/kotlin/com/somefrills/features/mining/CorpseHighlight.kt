package com.somefrills.features.mining

/*
import at.hannibal2.skyhanni.features.mining.glacitemineshaft.MineshaftWaypoint
import at.hannibal2.skyhanni.features.mining.glacitemineshaft.MineshaftWaypoints.waypoints
 */
import com.somefrills.config.FrillsMod
import com.somefrills.config.mining.MiningCategory.CorpseHighlightConfig
import com.somefrills.events.TickEventPre
import com.somefrills.features.core.AreaFeature
import com.somefrills.features.core.FrillsFeature
import com.somefrills.misc.Area
import com.somefrills.misc.RenderColor.Companion.fromChroma
import com.somefrills.utils.EntityUtils
import com.somefrills.utils.getEquippedArmor
import com.somefrills.utils.setGlowing
import com.somefrills.utils.toPlain
import io.github.notenoughupdates.moulconfig.ChromaColour
import meteordevelopment.orbit.EventHandler
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.item.ItemStack
import java.util.function.Predicate
import kotlin.jvm.java

@FrillsFeature
object CorpseHighlight : AreaFeature(FrillsMod.config.mining.corpseHighlight.enabled) {
    @JvmStatic
    val config: CorpseHighlightConfig
        get() = FrillsMod.config.mining.corpseHighlight

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
        val stands = EntityUtils.getStreamEntities(ArmorStand::class.java)
            .filter { stand: ArmorStand ->
                if (stand.isInvisible) return@filter false
                if (!stand.showArms()) return@filter false
                !stand.showBasePlate()
            }.toList()

        for (stand in stands) {
            val colour = getCorpseColor(getCorpseType(stand)) ?: continue
            val color = fromChroma(colour)
            stand.setGlowing(true, color)
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

    private fun getCorpseType(ent: ArmorStand): CorpseType {
        val armor = ent.getEquippedArmor()
        if (armor.isEmpty()) return CorpseType.None

        val helmet: ItemStack = armor[0]
        if (helmet.isEmpty) return CorpseType.None
        return when (helmet.displayName.toPlain()) {
            "Lapis Armor Helmet" -> CorpseType.Lapis
            "Mineral Helmet" -> CorpseType.Tungsten
            "Yog Helmet" -> CorpseType.Umber
            "Vanguard Helmet" -> CorpseType.Vanguard
            else -> CorpseType.None
        }
    }

/*
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
*/
}