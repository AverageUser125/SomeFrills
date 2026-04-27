package com.somefrills.features.mining;

import com.somefrills.config.FrillsConfig;
import com.somefrills.config.mining.MiningCategory.GhostVisionConfig;
import com.somefrills.events.EntityUpdatedEvent;
import com.somefrills.events.WorldRenderEvent;
import com.somefrills.features.core.AreaFeature;
import com.somefrills.misc.Area;
import com.somefrills.misc.EntityCache;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.util.math.Box;

public class GhostVision extends AreaFeature {

    private static final EntityCache cache = new EntityCache();
    private final GhostVisionConfig config;

    public GhostVision() {
        super(FrillsConfig.instance.mining.ghostVision.enabled);
        config = FrillsConfig.instance.mining.ghostVision;
    }

    @EventHandler
    private void onEntity(EntityUpdatedEvent event) {
        if (!(event.entity instanceof CreeperEntity creeper)) return;
        if (config.removeCharge) {
            creeper.getDataTracker().set(CreeperEntity.CHARGED, false);
        }
        cache.add(event.entity);
    }

    @EventHandler
    private void onRender(WorldRenderEvent event) {
        for (Entity ent : cache.get()) {
            if (!ent.isAlive()) continue;
            Box box = Utils.getLerpedBox(ent, event);
            event.drawStyled(box, config.style, false,
                    RenderColor.fromChroma(config.outline), RenderColor.fromChroma(config.fill));
        }
    }

    @Override
    protected boolean checkArea(Area area) {
        return area.equals(Area.DWARVEN_MINES);
    }
}
