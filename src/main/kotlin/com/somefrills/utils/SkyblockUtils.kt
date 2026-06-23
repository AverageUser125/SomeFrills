package com.somefrills.utils

import com.google.common.collect.Sets
import com.somefrills.Main.mc
import com.somefrills.misc.Area
import com.somefrills.misc.SkyblockData

object SkyblockUtils {
    private val lootIslands: HashSet<Area> = Sets.newHashSet(
        Area.CATACOMBS,
        Area.KUUDRA,
        Area.DUNGEON_HUB,
        Area.CRIMSON_ISLE
    )

    fun isOnHypixel(): Boolean {
        val info = mc.currentServer
        return info != null && info.ip.toLower().endsWith("hypixel.net")
    }


// ========== Skyblock State Extension Functions ==========

    fun isInZone(zone: String, containsCheck: Boolean = false): Boolean {
        return if (containsCheck) {
            SkyblockData.location.contains(zone)
        } else {
            SkyblockData.location.startsWith(zone)
        }
    }

    @JvmStatic
    fun isInArea(area: Area): Boolean {
        return SkyblockData.area == area
    }

    fun isInDungeons(): Boolean {
        return isInArea(Area.CATACOMBS)
    }

    fun isInLootArea(): Boolean {
        return lootIslands.contains(SkyblockData.area)
    }

    fun isInKuudra(): Boolean {
        return isInArea(Area.KUUDRA)
    }

    fun isInChateau(): Boolean {
        return isInZone(Symbols.zoneRift + " Stillgore Château", false) ||
                isInZone(Symbols.zoneRift + " Oubliette", false)
    }

    fun isOnPrivateIsland(): Boolean {
        return isInZone(Symbols.zone + " Your Island", false)
    }

    fun isOnGardenPlot(): Boolean {
        return SkyblockData.lines.stream().anyMatch { line -> line.contains("Plot -") }
    }

    fun isInGarden(): Boolean {
        return isInArea(Area.GARDEN)
    }

    fun isInHub(): Boolean {
        return isInArea(Area.HUB)
    }

    fun isInstanceOver(): Boolean {
        return SkyblockData.isInstanceOver
    }

    fun isInSkyblock(): Boolean {
        return SkyblockData.isInSkyblock
    }

    fun tabListLines(): List<String> {
        return SkyblockData.tabListLines
    }

    fun skyblockLocation(): String {
        return SkyblockData.location
    }

    val area: Area
        get() = SkyblockData.area
}
