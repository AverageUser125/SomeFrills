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

    @EventHandler
    public void onServerJoin(ServerJoinEvent event) {
        checkUpdate();
    }

    public static void checkUpdate() {
        if (hasCheckedThisSession) return;
        hasCheckedThisSession = true;

        if (!FrillsConfig.instance.about.fullAutoUpdates) {
            LOGGER.debug("Full auto updates is disabled");
            return;
        }

        // Start the update check asynchronously
        UpdateManager.checkUpdate();
    }
}