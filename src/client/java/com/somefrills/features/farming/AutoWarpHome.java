package com.somefrills.features.farming;

import com.somefrills.config.Feature;
import com.somefrills.events.WorldTickEvent;
import com.somefrills.events.ServerJoinEvent;
import com.somefrills.misc.SkyblockData;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;

public class AutoWarpHome {
    public static final Feature instance = new Feature("autoWarpHome");

    // Keep the last observed 'Alive' count from the tab list; -1 means unknown/not observed yet
    private static boolean wasZero = false;

    // Track last server join to implement a cooldown for automatic /home
    private static long lastServerJoinTime = 0L;
    private static final long JOIN_COOLDOWN_MS = 10_000L; // 10 seconds default

    @EventHandler
    private static void onWorldTick(WorldTickEvent event) {
        if (!instance.isActive() && Utils.isOnGardenPlot()) {
            return;
        }
        boolean tabDead = checkIfAliveIsZero();
        if (tabDead) {
            // Trigger only when count transitioned from >0 to 0
            if (!wasZero) {
                long now = System.currentTimeMillis();
                if (now - lastServerJoinTime > JOIN_COOLDOWN_MS) {
                    Utils.info("AutoWarpHome: transition >0->0 detected, running /home");
                    Utils.runCommand("home");
                }
            }
            wasZero = true;
        } else {
            wasZero = false;
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        lastServerJoinTime = System.currentTimeMillis();
    }

    private static boolean checkIfAliveIsZero() {
        return SkyblockData.getTabListLines().stream().anyMatch(line -> line.contains("Alive:") && line.contains("0"));
    }
}
