package com.somefrills.features.farming;

import com.somefrills.config.Feature;
import com.somefrills.events.ServerJoinEvent;
import com.somefrills.events.TabListUpdateEvent;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;

import java.util.List;

public class AutoWarpHome {
    public static final Feature instance = new Feature("autoWarpHome");

    private static PestStatus lastStatus = PestStatus.UNKNOWN; // -1 = unknown

    @EventHandler
    private static void onWorldTick(TabListUpdateEvent event) {
        if (!instance.isActive()) return;
        if (!Utils.isOnGardenPlot()) return;

        PestStatus status = checkAliveState(event.lines);
        if (status == PestStatus.UNKNOWN) return;

        // First valid reading → just store it
        if (lastStatus == PestStatus.UNKNOWN) {
            lastStatus = status;
            return;
        }

        // Trigger only on transition >0 → 0
        if (status == PestStatus.CLEARED && lastStatus == PestStatus.PRESENT) {
            Utils.info("AutoWarpHome: >0 → 0 detected, running /home");
            Utils.runCommand("home");
        }

        lastStatus = status;
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        lastStatus = PestStatus.UNKNOWN;
    }


    private static PestStatus checkAliveState(List<String> tabListLines) {
        for (String line : tabListLines) {
            if (line.contains("Alive")) {
                return line.contains("0") ? PestStatus.CLEARED : PestStatus.PRESENT;
            }
        }
        return PestStatus.UNKNOWN;
    }

    public enum PestStatus {
        UNKNOWN, PRESENT, CLEARED
    }
}