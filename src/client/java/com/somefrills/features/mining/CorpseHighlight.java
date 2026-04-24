package com.somefrills.features.mining;

import com.somefrills.config.FrillsConfig;
import com.somefrills.config.mining.MiningCategory.CorpseHighlightConfig;
import com.somefrills.events.InteractEntityEvent;
import com.somefrills.events.ServerJoinEvent;
import com.somefrills.events.WorldTickEvent;
import com.somefrills.features.core.AreaFeature;
import com.somefrills.misc.Area;
import com.somefrills.misc.ConcurrentHashSet;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Utils;
import io.github.notenoughupdates.moulconfig.ChromaColour;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

import java.util.Set;

import static com.somefrills.Main.mc;


public class CorpseHighlight extends AreaFeature {
    private final Set<Integer> openedCorpses = new ConcurrentHashSet<>();
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

    private static boolean hasKeyForCorpse(CorpseType type) {
        String id = switch (type) {
            case Tungsten -> "TUNGSTEN_KEY";
            case Umber -> "UMBER_KEY";
            case Vanguard -> "SKELETON_KEY";
            default -> "";
        };
        if (id.isEmpty()) return true;
        if (mc.player == null) return false;

        PlayerInventory inv = mc.player.getInventory();
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && Utils.getSkyblockId(stack).equals(id)) {
                return true;
            }
        }
        return false;
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
    private void onTick(WorldTickEvent event) {
        for (Entity ent : Utils.getEntities()) {
            if (!(ent instanceof ArmorStandEntity stand) || stand.isInvisible()) continue;
            if (openedCorpses.contains(stand.getId())) {
                continue;
            }
            var colour = getCorpseColor(getCorpseType(stand));
            if (colour == null) continue;
            RenderColor color = RenderColor.fromChroma(colour);
            Utils.setGlowing(stand, true, color);
        }
    }

    @EventHandler
    private void onInteractEntity(InteractEntityEvent event) {
        if (config.hideOpened && event.entity instanceof ArmorStandEntity stand) {
            CorpseType type = getCorpseType(stand);
            if (!type.equals(CorpseType.None) && hasKeyForCorpse(type)) {
                openedCorpses.add(stand.getId());
                Utils.setGlowing(stand, false, RenderColor.white);
            }
        }
    }

    @EventHandler
    private void onJoin(ServerJoinEvent event) {
        openedCorpses.clear();
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