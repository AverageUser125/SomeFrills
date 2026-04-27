package com.somefrills.features.mining;

import com.somefrills.config.FrillsConfig;
import com.somefrills.config.mining.MiningCategory.CorpseHighlightConfig;
import com.somefrills.events.TickEventPre;
import com.somefrills.features.core.AreaFeature;
import com.somefrills.misc.Area;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Utils;
import io.github.notenoughupdates.moulconfig.ChromaColour;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;

import java.util.List;

public class CorpseHighlight extends AreaFeature {
    private final CorpseHighlightConfig config;

    public CorpseHighlight() {
        super(FrillsConfig.instance.mining.corpseHighlight.enabled);
        config = FrillsConfig.instance.mining.corpseHighlight;
    }

    private static CorpseType getCorpseType(ArmorStandEntity ent) {
        var armor = Utils.getEntityArmor(ent);
        if (armor.isEmpty()) return CorpseType.None;

        ItemStack helmet = armor.getFirst();
        if (helmet.isEmpty()) return CorpseType.None;
        return switch (Utils.toPlain(helmet.getName())) {
            case "Lapis Armor Helmet" -> CorpseType.Lapis;
            case "Mineral Helmet" -> CorpseType.Tungsten;
            case "Yog Helmet" -> CorpseType.Umber;
            case "Vanguard Helmet" -> CorpseType.Vanguard;
            default -> CorpseType.None;
        };
    }


    private ChromaColour getCorpseColor(CorpseType type) {
        return switch (type) {
            case Lapis -> config.lapisColor;
            case Tungsten -> config.mineralColor;
            case Umber -> config.yogColor;
            case Vanguard -> config.vanguardColor;
            default -> null;
        };
    }


    @EventHandler
    private void onTick(TickEventPre event) {
        List<ArmorStandEntity> stands = Utils.getStreamEntities(ArmorStandEntity.class)
                .filter(stand -> {
                    if (stand.isInvisible()) return false;
                    if (!stand.shouldShowArms()) return false;
                    return !stand.shouldShowBasePlate();
                }).toList();

        for (ArmorStandEntity stand : stands) {
            var colour = getCorpseColor(getCorpseType(stand));
            if (colour == null) continue;
            RenderColor color = RenderColor.fromChroma(colour);
            Utils.setGlowing(stand, true, color);
        }
    }

    @Override
    protected boolean checkArea(Area area) {
        return area.equals(Area.MINESHAFT);
    }

    public enum CorpseType {
        Lapis,
        Tungsten,
        Umber,
        Vanguard,
        None
    }
}