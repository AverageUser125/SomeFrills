package com.somefrills.features.update;

import com.somefrills.config.FrillsConfig;
import com.somefrills.config.about.AboutCategory;
import com.somefrills.events.GameStartEvent;
import com.somefrills.features.core.Feature;
import meteordevelopment.orbit.EventHandler;

import static com.somefrills.Main.LOGGER;

public class AutoUpdate extends Feature {
    private final AboutCategory config;

    public AutoUpdate() {
        super(FrillsConfig.instance.about.checkForUpdates);
        config = FrillsConfig.instance.about;
    }

    @EventHandler
    public void onGameStart(GameStartEvent event) {
        LOGGER.debug("Performing automatic update check (autoQueue: {})", config.fullAutoUpdates);
        UpdateManager.checkUpdate(config.fullAutoUpdates);
    }
}