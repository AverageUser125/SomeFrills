package com.somefrills.features.fishing;

import java.util.Random;

import com.somefrills.config.Feature;
import com.somefrills.config.SettingBool;
import com.somefrills.config.SettingDescription;

import static com.somefrills.Main.mc;
import static com.somefrills.Main.LOGGER;

final class AutoFishAntiAfk {
    private static final Feature instance = new Feature("antiAfk");
    // Minimal state: last trigger time to debounce
    private static long lastAfkTriggerTime = 0;

    /**
     * Reset any anti-afk state. If rotate==true, reset player's view to default (0,0).
     */
    public static void reset(boolean rotate) {
        if (mc.player == null) return;
        if (rotate) {
            mc.player.setYaw(0f);
            mc.player.setPitch(0f);
        }
        lastAfkTriggerTime = 0;
    }

    /**
     * Apply a small random rotation immediately on the client. Debounced to once per minute.
     */
    public static void trigger() {
        if (!instance.isActive()) return;
        var player = mc.player;
        if (player == null) return;
        long now = System.currentTimeMillis();
        if (now - lastAfkTriggerTime < 60_000) return;
        lastAfkTriggerTime = now;
        Random rand = new Random();
        float yawDelta = -5f + rand.nextFloat() * 10f;   // -5 to +5 degrees
        float pitchDelta = -2f + rand.nextFloat() * 4f;  // -2 to +2 degrees
        player.setYaw((player.getYaw() + yawDelta) % 360);
        player.setPitch(Math.max(-90f, Math.min(90f, player.getPitch() + pitchDelta)));
        LOGGER.debug("AutoFishAntiAfk.trigger applied yawDelta={}, pitchDelta={}", yawDelta, pitchDelta);
    }
}
