package com.somefrills.features.solvers;

import com.somefrills.config.Feature;
import com.somefrills.events.ClientDisconnectEvent;
import com.somefrills.events.EndTickEvent;
import com.somefrills.events.WorldTickEvent;
import com.somefrills.misc.GlowManager;
import com.somefrills.misc.GlowTeamManager;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Formatting;

import static com.somefrills.Main.mc;

public class GlowPlayer {
    public static final Feature instance = new Feature("glowPlayer", true);

    @EventHandler
    public static void onWorldTick(EndTickEvent event) {
        if (!instance.isActive()) return;
        if (mc.world == null) return;
        GlowTeamManager.init();
        for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
            Formatting color = GlowManager.getColor(player.getUuid());
            if (color != null) {
                GlowTeamManager.assign(player.getName().getString(), color);
            }
        }
    }

    @EventHandler
    public static void onDisconnect(ClientDisconnectEvent event) {
        if (!instance.isActive()) return;
        GlowManager.clear();
        GlowTeamManager.clear();
    }
}
