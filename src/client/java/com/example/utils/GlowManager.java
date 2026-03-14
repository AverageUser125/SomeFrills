package com.example.utils;

import net.minecraft.util.Formatting;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GlowManager {

    private static final Map<UUID, Formatting> forcedGlows = new HashMap<>();

    // Add a player with a color
    public static boolean add(UUID uuid, Formatting color) {
        if (uuid == null || color == null) return false;
        return forcedGlows.put(uuid, color) == null;
    }

    // Remove a player
    public static boolean remove(UUID uuid) {
        if (uuid == null) return false;
        return forcedGlows.remove(uuid) != null;
    }

    // Check if player has glow
    public static boolean has(UUID uuid) {
        if (uuid == null) return false;
        return forcedGlows.containsKey(uuid);
    }

    // Get glow color (null if not glowing)
    public static Formatting getColor(UUID uuid) {
        return uuid == null ? null : forcedGlows.get(uuid);
    }

    // Clear all forced glows
    public static void clear() {
        forcedGlows.clear();
    }

    // Read-only view
    public static Map<UUID, Formatting> getAll() {
        return Collections.unmodifiableMap(forcedGlows);
    }
}
