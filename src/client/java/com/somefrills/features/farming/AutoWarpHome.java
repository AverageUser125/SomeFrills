package com.somefrills.features.farming;

import com.somefrills.config.Feature;
import com.somefrills.events.WorldTickEvent;
import com.somefrills.misc.SkyblockData;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoWarpHome {
    public static final Feature instance = new Feature("autoWarpHome");

    // Keep the last observed 'Alive' count from the tab list; -1 means unknown/not observed yet
    private static int previousAliveCount = -1;
    private static final Pattern ALIVE_PATTERN = Pattern.compile("Alive:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

    @EventHandler
    private static void onWorldTick(WorldTickEvent event) {
        if (!instance.isActive() && Utils.isOnGardenPlot()) {
            return;
        }

        int tabAlive = findAliveCountInTab(SkyblockData.getTabListLines());

        if (tabAlive >= 0) {
            // Trigger only when count transitioned from >0 to 0
            if (tabAlive == 0 && previousAliveCount > 0) {
                Utils.runCommand("home");
            }
            previousAliveCount = tabAlive;
        }
        // if tabAlive < 0, we don't update previousAliveCount (lack of data)
    }

    private static int findAliveCountInTab(List<String> lines) {
        if (lines == null) return -1;
        for (String line : lines) {
            if (line == null) continue;
            String stripped = Formatting.strip(line).trim();
            Matcher m = ALIVE_PATTERN.matcher(stripped);
            if (m.find()) {
                try {
                    return Integer.parseInt(m.group(1));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return -1;
    }
}
