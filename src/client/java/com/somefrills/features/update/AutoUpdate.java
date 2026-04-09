package com.somefrills.features.update;

import com.somefrills.config.Feature;
import com.somefrills.config.FrillsConfig;
import com.somefrills.events.ServerJoinEvent;
import meteordevelopment.orbit.EventHandler;

import static com.somefrills.Main.LOGGER;

public class AutoUpdate extends Feature {

    private static boolean hasCheckedThisSession = false;

    public AutoUpdate() {
        super(FrillsConfig.instance.about.checkForUpdates);
    }

    public static void checkUpdate() {
        if (hasCheckedThisSession) return;
        hasCheckedThisSession = true;

        if (!FrillsConfig.instance.about.checkForUpdates.get()) {
            LOGGER.debug("Check for updates is disabled");
            return;
        }

        boolean autoQueue = FrillsConfig.instance.about.fullAutoUpdates;
        LOGGER.debug("Performing automatic update check (autoQueue: {})", autoQueue);
        UpdateManager.checkUpdate(autoQueue);
    }

    @EventHandler
    public void onServerJoin(ServerJoinEvent event) {
        checkUpdate();
    }
}