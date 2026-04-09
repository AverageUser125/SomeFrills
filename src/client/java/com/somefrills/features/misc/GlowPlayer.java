package com.somefrills.features.misc;

import com.somefrills.config.Feature;
import com.somefrills.config.FrillsConfig;
import com.somefrills.events.EntityUpdatedEvent;
import com.somefrills.events.ServerJoinEvent;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;

import java.util.concurrent.ConcurrentHashMap;

public class GlowPlayer extends Feature {
    private static final ConcurrentHashMap<String, RenderColor> forcedGlows = new ConcurrentHashMap<>();

    public GlowPlayer() {
        super(FrillsConfig.instance.misc.glowPlayer.enabled);
    }

    public static String convertToPureName(PlayerEntity player) {
        if (player == null) return null;
        return Formatting.strip(player.getGameProfile().name());
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
        if (!(entity instanceof PlayerEntity player)) return;
        String pureName = convertToPureName(player);
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
