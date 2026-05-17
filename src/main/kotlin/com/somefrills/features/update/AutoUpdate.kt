package com.somefrills.features.update

import com.somefrills.Main.LOGGER
import com.somefrills.config.FrillsMod

import com.somefrills.events.GameStartEvent
import com.somefrills.features.core.Feature
import com.somefrills.features.core.FrillsFeature
import meteordevelopment.orbit.EventHandler

@FrillsFeature
class AutoUpdate : Feature(FrillsMod.config.about.checkForUpdates) {
    private val config get() = FrillsMod.config.about

    @EventHandler
    fun onGameStart(event: GameStartEvent) {
        LOGGER.debug("Performing automatic update check (autoQueue: {})", config.fullAutoUpdates)
        UpdateManager.checkUpdate(config.fullAutoUpdates)
    }
}