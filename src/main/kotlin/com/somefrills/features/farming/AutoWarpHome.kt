package com.somefrills.features.farming

import com.somefrills.config.FrillsMod

import com.somefrills.events.ServerJoinEvent
import com.somefrills.events.TabListUpdateEvent
import com.somefrills.features.core.AreaFeature
import com.somefrills.features.core.FrillsFeature
import com.somefrills.misc.Area
import com.somefrills.misc.Utils
import meteordevelopment.orbit.EventHandler

@FrillsFeature
object AutoWarpHome : AreaFeature(FrillsMod.config.farming.autoWarpHomeEnabled) {
    private var lastStatus: PestStatus? = PestStatus.UNKNOWN // -1 = unknown

    private fun checkAliveState(tabListLines: MutableList<String>): PestStatus {
        for (line in tabListLines) {
            if (line.contains("Alive")) {
                return if (line.contains("0")) PestStatus.CLEARED else PestStatus.PRESENT
            }
        }
        return PestStatus.UNKNOWN
    }

    @EventHandler
    private fun onWorldTick(event: TabListUpdateEvent) {
        val status: PestStatus = checkAliveState(event.lines)
        if (status == PestStatus.UNKNOWN) return

        // First valid reading → just store it
        if (lastStatus == PestStatus.UNKNOWN) {
            lastStatus = status
            return
        }

        // Trigger only on transition >0 → 0
        if (status == PestStatus.CLEARED && lastStatus == PestStatus.PRESENT) {
            Utils.info("AutoWarpHome: >0 → 0 detected, running /home")
            Utils.runCommand("home")
        }

        lastStatus = status
    }

    @EventHandler
    private fun onJoin(event: ServerJoinEvent) {
        lastStatus = PestStatus.UNKNOWN
    }

    override fun checkArea(area: Area): Boolean {
        return area == Area.GARDEN && Utils.isOnGardenPlot()
    }

    enum class PestStatus {
        UNKNOWN, PRESENT, CLEARED
    }
}