package com.somefrills.features.update

import com.somefrills.Main.LOGGER
import com.somefrills.config.FrillsMod

import com.somefrills.events.GameStartEvent
import com.somefrills.features.core.Feature
import com.somefrills.modules.FrillsFeature
import com.somefrills.events.core.EventHandle

@FrillsFeature
object AutoUpdate : Feature(FrillsMod.config.about.checkForUpdates) {
    private val config get() = FrillsMod.config.about

    @EventHandle
    fun onGameStart(event: GameStartEvent) {
        LOGGER.debug("Performing automatic update check (autoQueue: {})", config.fullAutoUpdates)
        UpdateManager.checkUpdate(config.fullAutoUpdates)
    }
}