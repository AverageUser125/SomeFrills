package com.somefrills.features.misc

import com.google.gson.JsonParser
import com.somefrills.Main.mc
import com.somefrills.config.FrillsMod

import com.somefrills.events.TickEventPost
import com.somefrills.events.WorldRenderEvent
import com.somefrills.features.core.Feature
import com.somefrills.features.core.FrillsFeature
import com.somefrills.misc.Area
import com.somefrills.misc.RenderColor
import com.somefrills.misc.SkyblockData
import com.somefrills.utils.ChatUtils
import io.github.notenoughupdates.moulconfig.ChromaColour
import meteordevelopment.orbit.EventHandler
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import java.io.IOException
import java.nio.file.Files
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@FrillsFeature
object NpcLocator : Feature(FrillsMod.config.misc.npcLocator.enabled) {
    private val config get() = FrillsMod.config.misc.npcLocator

    private val npcLocations = ConcurrentHashMap<String, NpcLocation>()
    private var color = RenderColor(255, 100, 100, 255)
    private var cachedIsland: Area? = null
    private var cachedNpcs: MutableMap<String, Vec3> = HashMap<String, Vec3>()
    private fun onColorConfigChanged(newColor: ChromaColour) {
        color = RenderColor.fromChroma(newColor)
    }

    init {
        config.color.addObserver { oldVal: ChromaColour, newVal: ChromaColour -> onColorConfigChanged(newVal) }
        onColorConfigChanged(config.color.get())
    }

    @EventHandler
    fun onWorldTick(event: TickEventPost?) {
        if (!config.autoRemoveWaypoint) return
        val player = mc.player ?: return
        if (npcLocations.isEmpty()) return

        val playerPos: Vec3 = player.eyePosition
        npcLocations.entries.removeIf { entry: MutableMap.MutableEntry<String, NpcLocation> ->
            val npcPos = entry.value.position
            val distance = playerPos.distanceTo(npcPos)
            distance <= config.waypointRemoveDistance
        }
    }

    @EventHandler
    fun onRenderEvent(event: WorldRenderEvent) {
        for (npc in npcLocations.values) {
            val center = npc.position.add(0.5, 0.0, 0.5)
            if (config.beaconBeam) {
                event.drawBeam(center, 255, true, color)
            }
            if (config.tracer) {
                event.drawTracer(center, color)
            }
            if (config.outlineBox) {
                val box = AABB(npc.position.subtract(0.0, 1.0, 0.0), npc.position.add(1.0, 1.0, 1.0))
                event.drawOutline(box, true, color)
            }
        }
    }

    @JvmStatic
    fun addNpcLocation(npcName: String) {
        val location: Vec3? = getNpcCoordinates(npcName)
        if (location != null) {
            npcLocations[npcName] = NpcLocation(npcName, location)
            ChatUtils.infoFormat("Added {} to NPC Locator.", npcName)
        } else {
            ChatUtils.infoFormat("Could not find NPC: {}", npcName)
        }
    }

    @JvmStatic
    fun removeNpcLocation(npcName: String) {
        npcLocations.remove(npcName)
        ChatUtils.infoFormat("Removed {} from NPC Locator.", npcName)
    }

    @JvmStatic
    fun clearAllNpcLocations() {
        npcLocations.clear()
        ChatUtils.info("Cleared all NPC locations.")
    }

    @JvmStatic
    fun getAllNpcLocations(): Collection<NpcLocation> {
        return Collections.unmodifiableCollection(npcLocations.values)
    }

    @JvmStatic
    fun getAvailableNpcsForCurrentIsland(): Collection<String> {
        ensureCacheLoaded()
        return cachedNpcs.keys
    }

    private fun ensureCacheLoaded() {
        val currentIsland = SkyblockData.area
        if (cachedIsland != currentIsland) {
            cachedIsland = currentIsland
            cachedNpcs = loadIslandNpcs(currentIsland)
        }
    }

    private fun getNpcCoordinates(npcName: String?): Vec3? {
        ensureCacheLoaded()
        return cachedNpcs[npcName]
    }

    private fun loadIslandNpcs(area: Area): MutableMap<String, Vec3> {
        val npcs: MutableMap<String, Vec3> = HashMap<String, Vec3>()

        val locationFileName = area.displayName.replace(" ", "_").uppercase(Locale.getDefault()) + ".json"
        val locationFile = FabricLoader.getInstance().configDir
            .resolve("skyhanni/repo/constants/island_graphs/$locationFileName")

        if (!Files.exists(locationFile)) {
            return npcs
        }

        try {
            val jsonContent = Files.readString(locationFile)
            val jsonObject = JsonParser.parseString(jsonContent).getAsJsonObject()

            for (key in jsonObject.keySet()) {
                val element = jsonObject.get(key)
                if (!element.isJsonObject) continue

                val node = element.getAsJsonObject()

                // Check if this node has the "npc" tag
                var isNpc = false
                if (node.has("Tags") && node.get("Tags").isJsonArray) {
                    val tagsArray = node.getAsJsonArray("Tags")
                    if (tagsArray != null) {
                        for (tag in tagsArray) {
                            if (tag.isJsonPrimitive && tag.asString == "npc") {
                                isNpc = true
                                break
                            }
                        }
                    }
                }

                if (!isNpc) continue

                // Extract NPC name and position
                if (!node.has("Name") || !node.has("Position")) continue

                val npcName = node.get("Name").asString
                val positionStr = node.get("Position").asString

                val position: Vec3? = parsePosition(positionStr)
                if (position != null) {
                    npcs[npcName] = position
                }
            }
        } catch (e: IOException) {
            // Log error if needed
        } catch (e: RuntimeException) {
        }

        return npcs
    }

    private fun parsePosition(positionStr: String): Vec3? {
        try {
            val parts: Array<String> =
                positionStr.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (parts.size != 3) return null

            val x = parts[0].toDouble()
            val y = parts[1].toDouble()
            val z = parts[2].toDouble()

            return Vec3(x, y, z)
        } catch (e: NumberFormatException) {
            return null
        }
    }

    @JvmRecord
    data class NpcLocation(@JvmField val npcName: String, val position: Vec3)
}