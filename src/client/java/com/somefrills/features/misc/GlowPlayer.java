package com.somefrills.features.misc;

import com.somefrills.config.Feature;
import com.somefrills.config.FrillsConfig;
import com.somefrills.events.EntityUpdatedEvent;
import com.somefrills.events.ServerJoinEvent;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.Formatting;

import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlowPlayer extends Feature {
    private static final ConcurrentHashMap<String, RenderColor> forcedGlows = new ConcurrentHashMap<>();
    private static final Pattern USERNAME_TOKEN = Pattern.compile("[A-Za-z0-9_]{1,16}");

    public GlowPlayer() {
        super(FrillsConfig.instance.misc.glowPlayer.enabled);
    }

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

    @EventHandler
    private void onServerJoin(ServerJoinEvent event) {
        for (Entity entity : Utils.getEntities()) {
            applyHighlight(entity);
        }
    }

    @EventHandler
    public void onEntityUpdate(EntityUpdatedEvent event) {
        if (!isActive()) return;
        var entity = event.entity;
        applyHighlight(entity);
    }

    private static void applyHighlight(Entity entity) {
        if (!(entity instanceof AbstractClientPlayerEntity player)) return;
        String pureName = convertToPureName(player.getName().getString());
        RenderColor color = getColor(pureName);
        if (color != null) {
            Utils.setGlowing(entity, true, color);
        }
    }

    public static boolean addPlayer(String pureName, RenderColor color) {
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

    public static RenderColor getColor(String pureName) {
        if (pureName == null) return null;
        return forcedGlows.get(pureName);
    }

    public static void clear() {
        forcedGlows.clear();
    }

    public static java.util.Set<String> getForcedNames() {
        return java.util.Set.copyOf(forcedGlows.keySet());
    }

    public static void setGlowImmediately(AbstractClientPlayerEntity player, RenderColor color) {
        Utils.setGlowing(player, true, color);
    }
}
