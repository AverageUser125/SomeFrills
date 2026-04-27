package com.somefrills.features.farming;

import com.somefrills.config.FrillsConfig;
import com.somefrills.events.ServerJoinEvent;
import com.somefrills.events.TabListUpdateEvent;
import com.somefrills.features.core.AreaFeature;
import com.somefrills.misc.Area;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;

import java.util.List;

public class AutoWarpHome extends AreaFeature {
    private static PestStatus lastStatus = PestStatus.UNKNOWN; // -1 = unknown

    public AutoWarpHome() {
        super(FrillsConfig.instance.farming.autoWarpHomeEnabled);
    }

    private static PestStatus checkAliveState(List<String> tabListLines) {
        for (String line : tabListLines) {
            if (line.contains("Alive")) {
                return line.contains("0") ? PestStatus.CLEARED : PestStatus.PRESENT;
            }
        }
        return PestStatus.UNKNOWN;
    }

    @EventHandler
    private void onWorldTick(TabListUpdateEvent event) {
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
    private void onJoin(ServerJoinEvent event) {
        lastStatus = PestStatus.UNKNOWN;
    }

    @Override
    protected boolean checkArea(Area area) {
        return area.equals(Area.GARDEN) && Utils.isOnGardenPlot();
    }

    public enum PestStatus {
        UNKNOWN, PRESENT, CLEARED
    }
}