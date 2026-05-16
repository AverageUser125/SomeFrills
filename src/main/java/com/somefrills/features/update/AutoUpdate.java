package com.somefrills.features.update;

import com.somefrills.config.FrillsConfig;
import com.somefrills.config.about.AboutCategory;
import com.somefrills.events.GameStartEvent;
import com.somefrills.features.core.Feature;
import com.somefrills.features.core.FrillsFeature;
import meteordevelopment.orbit.EventHandler;

import static com.somefrills.Main.LOGGER;

@FrillsFeature
public class AutoUpdate extends Feature {
    private final AboutCategory config;

    public AutoUpdate() {
        super(FrillsConfig.about.checkForUpdates);
        config = FrillsConfig.about;
    }

    @EventHandler
    public void onGameStart(GameStartEvent event) {
        LOGGER.debug("Performing automatic update check (autoQueue: {})", config.fullAutoUpdates);
        UpdateManager.checkUpdate(config.fullAutoUpdates);
    }
}