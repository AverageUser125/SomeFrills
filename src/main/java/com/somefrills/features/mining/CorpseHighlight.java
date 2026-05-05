package com.somefrills.features.mining;

import at.hannibal2.skyhanni.features.mining.glacitemineshaft.MineshaftWaypoint;
import at.hannibal2.skyhanni.features.mining.glacitemineshaft.MineshaftWaypoints;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

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


    private static List<String> shareAllWaypoints(Predicate<MineshaftWaypoint> filter) {
        ArrayList<String> sb = new ArrayList<>();
        var waypoints = MineshaftWaypoints.INSTANCE.getWaypoints();
        for (MineshaftWaypoint waypoint : waypoints) {
            if (!filter.test(waypoint)) continue;
            var location = waypoint.getLocation().toChatFormat();
            var type = waypoint.getWaypointType().getDisplayText();

            String message = String.format("%s | (%s)", location, type);
            sb.add(message);
            waypoint.setShared(true);
        }
        return sb;
    }

    public static List<String> shareAllWaypointsForce() {
        return shareAllWaypoints(MineshaftWaypoint::isCorpse);
    }

    public static List<String> shareAllWaypoints() {
        return shareAllWaypoints(waypoint -> !waypoint.getShared() && waypoint.isCorpse());
    }
}