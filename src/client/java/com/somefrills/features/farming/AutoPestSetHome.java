package com.somefrills.features.farming;

import com.somefrills.config.Feature;
import com.somefrills.events.ChatMsgEvent;
import com.somefrills.events.ServerJoinEvent;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Formatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.somefrills.Main.mc;

public class AutoPestSetHome {
    public static final Feature instance = new Feature("autoPestSetHome");

    private static long lastServerJoinTime = 0L;
    private static final long IGNORE_WINDOW_MS = 10_000L;

    private static final Pattern PEST_SPAWN_PATTERN = Pattern.compile(
            "\\bPest[s]?\\b.*?spawn(?:ed)?\\b.*?Plot\\s*-?\\s*\\d+",
            Pattern.CASE_INSENSITIVE
    );

    @EventHandler
    private static void onServerJoin(ServerJoinEvent event) {
        if (!instance.isActive()) return;
        lastServerJoinTime = System.currentTimeMillis();
    }

    @EventHandler
    private static void onChatMessage(ChatMsgEvent event) {
        if (!instance.isActive()) return;
        if (!Utils.isOnGardenPlot()) return;
        if (event.messagePlain == null || event.messagePlain.isEmpty()) return;

        // Strip formatting safely
        String stripped = Formatting.strip(event.messagePlain);
        stripped = stripped.trim();

        Matcher m = PEST_SPAWN_PATTERN.matcher(stripped);
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
}