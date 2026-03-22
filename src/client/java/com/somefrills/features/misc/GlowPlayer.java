package com.somefrills.features.misc;

import com.somefrills.config.Feature;
import com.somefrills.events.ClientDisconnectEvent;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.ColorHelper;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.somefrills.Main.mc;

public class GlowPlayer {
    public static final Feature instance = new Feature("glowPlayer", true);
    private static final ConcurrentHashMap<String, Formatting> forcedGlows = new ConcurrentHashMap<>();
    private static final Pattern USERNAME_TOKEN = Pattern.compile("[A-Za-z0-9_]{1,16}");

    /**
     * Normalize an arbitrary string to a likely pure username. Strips formatting codes and attempts
     * to extract a token that matches typical username characters (alphanumeric + underscore).
     * If none is found, returns the stripped full string.
     */
    public static String convertToPureName(String raw) {
        if (raw == null) return null;
        String stripped = Formatting.strip(raw).trim();
        if (stripped.isEmpty()) return null;

        // If the whole stripped string looks like a username, return it
        Matcher whole = USERNAME_TOKEN.matcher(stripped);
        if (whole.matches()) return stripped;

        // Otherwise, try to find a token within the string that looks like a username
        for (String token : stripped.split("\\s+")) {
            Matcher m = USERNAME_TOKEN.matcher(token);
            if (m.matches()) return token;
        }

        // Fallback: return the stripped string (best effort)
        return stripped;
    }
    public static void onDisconnect(ClientDisconnectEvent event) {
        if (!instance.isActive()) return;
        clear();
    }

    // API: operate by pure player name (String) only; a color is required to add
    public static boolean addPlayer(String pureName, Formatting color) {
        if (pureName == null || color == null) return false;
        return forcedGlows.put(pureName, color) == null;
    }

    public static boolean removePlayer(String pureName) {
        if (pureName == null) return false;
        return forcedGlows.remove(pureName) != null;
    }

    public static boolean hasPlayer(String pureName) {
        if (pureName == null) return false;
        return forcedGlows.containsKey(pureName);
    }

    public static Formatting getColor(String pureName) {
        if (pureName == null) return null;
        return forcedGlows.get(pureName);
    }

    public static void clear() {
        forcedGlows.clear();
    }

    public static java.util.Set<String> getForcedNames() {
        return java.util.Set.copyOf(forcedGlows.keySet());
    }
    public static Integer getColorAsInt(String pureName) {
        Formatting f = getColor(pureName);
        if(f == null) return null;
        Integer color = f.getColorValue();
        if(color == null) return null;
        return ColorHelper.fullAlpha(color);
    }
}
