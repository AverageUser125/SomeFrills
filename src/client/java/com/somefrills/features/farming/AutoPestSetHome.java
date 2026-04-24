package com.somefrills.features.farming;

import com.somefrills.config.FrillsConfig;
import com.somefrills.events.ChatMsgEvent;
import com.somefrills.events.ServerJoinEvent;
import com.somefrills.features.core.AreaFeature;
import com.somefrills.misc.Area;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.somefrills.Main.mc;

public class AutoPestSetHome extends AreaFeature {
    private static final long IGNORE_WINDOW_MS = 10_000L;
    private static final Pattern PEST_SPAWN_PATTERN = Pattern.compile(
            "\\bPest[s]?\\b.*?spawn(?:ed)?\\b.*?Plot\\s*-?\\s*\\d+",
            Pattern.CASE_INSENSITIVE
    );
    private static long lastServerJoinTime = 0L;

    public AutoPestSetHome() {
        super(FrillsConfig.instance.farming.autoPestSetHomeEnabled);
    }

    @EventHandler
    private void onServerJoin(ServerJoinEvent event) {
        lastServerJoinTime = System.currentTimeMillis();
    }

    @EventHandler
    private void onChatMessage(ChatMsgEvent event) {
        if (event.messagePlain == null || event.messagePlain.isEmpty()) return;
        Matcher m = PEST_SPAWN_PATTERN.matcher(event.messagePlain);
        if (!m.find()) return;

        long now = System.currentTimeMillis();
        if (now - lastServerJoinTime < IGNORE_WINDOW_MS) return;

        if (mc.player == null || mc.player.networkHandler == null) return;

        try {
            Utils.info("Pests spawned detected in chat, running /sethome");
            Utils.runCommand("sethome");
        } catch (Exception ignored) {
        }
    }

    @Override
    protected boolean checkArea(Area area) {
        return area.equals(Area.GARDEN) && Utils.isOnGardenPlot();
    }
}