package com.somefrills.features.mining;

import com.somefrills.config.Feature;
import com.somefrills.config.FrillsConfig;
import com.somefrills.config.mining.MiningCategory.GhostVisionConfig;
import com.somefrills.events.AreaChangeEvent;
import com.somefrills.events.EntityUpdatedEvent;
import com.somefrills.events.WorldRenderEvent;
import com.somefrills.misc.Area;
import com.somefrills.misc.EntityCache;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.util.math.Box;

public class GhostVision extends Feature {

    private static final EntityCache cache = new EntityCache();
    private static boolean inDwarvenMines = false;
    private final GhostVisionConfig config;

    public GhostVision() {
        super(FrillsConfig.instance.mining.ghostVision.enabled);
        config = FrillsConfig.instance.mining.ghostVision;
    }


    public static boolean isGhost(CreeperEntity creeper) {
        return creeper.getY() < 100 && creeper.isCharged();
    }

    @EventHandler
    private void onEntity(EntityUpdatedEvent event) {
        if (!isActive()) return;
        if (!inDwarvenMines) return;
        if (!(event.entity instanceof CreeperEntity creeper)) return;
        if (!isGhost(creeper)) return;
        if (config.removeCharge) {
            creeper.getDataTracker().set(CreeperEntity.CHARGED, false);
        }
        cache.add(event.entity);
    }


    @EventHandler
    private void onScoreboardUpdate(AreaChangeEvent event) {
        inDwarvenMines = event.area.equals(Area.DWARVEN_MINES);
    }

    @EventHandler
    private void onRender(WorldRenderEvent event) {
        if (!inDwarvenMines) return;
        for (Entity ent : cache.get()) {
            if (!ent.isAlive()) continue;
            Box box = Utils.getLerpedBox(ent, event);
            event.drawStyled(box, config.style, false,
                    RenderColor.fromChroma(config.outline), RenderColor.fromChroma(config.fill));
        }
    }

}
