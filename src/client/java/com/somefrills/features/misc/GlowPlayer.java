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
import net.minecraft.entity.player.PlayerEntity;

import java.util.concurrent.ConcurrentHashMap;

public class GlowPlayer extends Feature {
    private static final ConcurrentHashMap<String, RenderColor> forcedGlows = new ConcurrentHashMap<>();

    public GlowPlayer() {
        super(FrillsConfig.instance.misc.glowPlayer.enabled);
    }

    private static void applyHighlight(Entity entity) {
        if (!(entity instanceof PlayerEntity player)) return;
        String pureName = Utils.getPlayerName(player);
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
        if (forcedGlows.remove(pureName) == null) {
            return false;
        }
        for (Entity entity : Utils.getEntities()) {
            if (!(entity instanceof PlayerEntity player)) continue;
            String entityPureName = Utils.getPlayerName(player);
            if (!pureName.equals(entityPureName)) continue;
            Utils.setGlowing(entity, false, RenderColor.white);
            break;
        }
        return true;
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
        for (Entity entity : Utils.getEntities()) {
            if (!(entity instanceof PlayerEntity player)) continue;
            String pureName = Utils.getPlayerName(player);
            if (pureName == null || !forcedGlows.containsKey(pureName)) continue;
            Utils.setGlowing(entity, false, RenderColor.white);
        }
        forcedGlows.clear();
    }

    public static java.util.Set<String> getForcedNames() {
        return java.util.Set.copyOf(forcedGlows.keySet());
    }

    public static void setGlowImmediately(AbstractClientPlayerEntity player, RenderColor color) {
        Utils.setGlowing(player, true, color);
    }

    @EventHandler
    private void onServerJoin(ServerJoinEvent event) {
        for (Entity entity : Utils.getEntities()) {
            applyHighlight(entity);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityUpdate(EntityUpdatedEvent event) {
        if (!isActive()) return;
        var entity = event.entity;
        applyHighlight(entity);
    }
}
