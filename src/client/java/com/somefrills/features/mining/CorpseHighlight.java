package com.somefrills.features.mining;

import com.somefrills.config.Feature;
import com.somefrills.config.FrillsConfig;
import com.somefrills.config.mining.MiningCategory.CorpseHighlightConfig;
import com.somefrills.events.AreaChangeEvent;
import com.somefrills.events.CorpseEvent;
import com.somefrills.events.WorldTickEvent;
import com.somefrills.features.mining.CorpseApi.Corpse;
import com.somefrills.features.mining.CorpseApi.CorpseType;
import com.somefrills.misc.Area;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Utils;
import io.github.notenoughupdates.moulconfig.ChromaColour;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.LivingEntity;

import static com.somefrills.Main.eventBus;

public class CorpseHighlight extends Feature {
    private final CorpseHighlightConfig config;
    private Delegate delegate = null;

    public CorpseHighlight() {
        super(FrillsConfig.instance.mining.corpseHighlight.enabled);
        config = FrillsConfig.instance.mining.corpseHighlight;
    }

    public RenderColor getCorpseColor(CorpseType type) {
        ChromaColour colour = switch (type) {
            case Lapis -> config.lapisColor;
            case Tungsten -> config.mineralColor;
            case Umber -> config.yogColor;
            case Vanguard -> config.vanguardColor;
            default -> null;
        };
        return RenderColor.fromChroma(colour);
    }

    private boolean active() {
        return isActive() && Utils.isInArea(Area.MINESHAFT);
    }

    @EventHandler
    private void onCorpse(CorpseEvent event) {
        if (delegate != null) return;
        delegate = new Delegate();
        eventBus.subscribe(delegate);
    }

    private class Delegate {
        @EventHandler
        private void onTick(WorldTickEvent event) {
            if (!active()) return;
            for (Corpse corpse : CorpseApi.getCorpses()) {
                LivingEntity entity = corpse.getEntity();
                if (entity == null) continue;

                if (corpse.isOpened()) {
                    Utils.setGlowing(entity, false, RenderColor.white);
                    continue;
                }
                RenderColor color = getCorpseColor(corpse.getType());
                Utils.setGlowing(entity, true, color);
            }
        }

        @EventHandler
        private void onAreaChange(AreaChangeEvent event) {
            if (Utils.isInArea(Area.MINESHAFT)) return;
            eventBus.unsubscribe(this);
            delegate = null;
        }
    }

}