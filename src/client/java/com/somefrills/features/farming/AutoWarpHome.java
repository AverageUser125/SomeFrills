package com.somefrills.features.farming;

import com.somefrills.config.Feature;
import com.somefrills.events.WorldTickEvent;
import com.somefrills.misc.SkyblockData;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import static com.somefrills.Main.LOGGER;
public class AutoWarpHome {
    public static final Feature instance = new Feature("autoWarpHome");

    // Keep the last observed 'Alive' count from the tab list; -1 means unknown/not observed yet
    private static boolean wasZero = false;

    @EventHandler
    private static void onWorldTick(WorldTickEvent event) {
        if (!instance.isActive() && Utils.isOnGardenPlot()) {
            return;
        }
        boolean tabDead = checkIfAliveIsZero();
        if (tabDead) {
            // Trigger only when count transitioned from >0 to 0
            if (!wasZero) {
                Utils.info("AutoWarpHome: transition >0->0 detected, running /home");
                Utils.runCommand("home");
            }
            wasZero = true;
        } else {
            wasZero = false;
        }
    }

    private static boolean checkIfAliveIsZero() {
        return SkyblockData.getTabListLines().stream().anyMatch(line -> line.contains("Alive:") && line.contains("0"));
    }
}
