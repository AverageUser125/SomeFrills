package com.somefrills.features.mining;

import com.somefrills.events.AreaChangeEvent;
import com.somefrills.events.CorpseEvent;
import com.somefrills.events.InteractEntityEvent;
import com.somefrills.events.ServerJoinEvent;
import com.somefrills.misc.Area;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

import java.util.*;

import static com.somefrills.Main.eventBus;
import static com.somefrills.Main.mc;

public class CorpseApi {
    private static final List<Corpse> corpses = new ArrayList<>();
    private static final Set<UUID> knownCorpses = new HashSet<>();

    public static class Delegate {
        Area prevArea = null;

        @EventHandler
        private void onAreaChange(AreaChangeEvent event) {
            if (event.area == Area.MINESHAFT) {
                eventBus.subscribe(CorpseApi.class);
            } else if (prevArea == Area.MINESHAFT) {
                eventBus.unsubscribe(CorpseApi.class);
                corpses.clear();
                knownCorpses.clear();
            }
            prevArea = event.area;
        }
    }

    // -------------------------
    // PUBLIC API
    // -------------------------

    public static List<Corpse> getCorpses() {
        return Collections.unmodifiableList(corpses);
    }

    // -------------------------
    // UPDATE LOOP
    // -------------------------

    public static void updateCorpses() {
        corpses.clear();

        for (Entity ent : Utils.getEntities()) {
            if (!(ent instanceof ArmorStandEntity stand)) continue;
            if (stand.isInvisible()) continue;
            if (!stand.shouldShowArms()) continue;
            if (!stand.shouldShowBasePlate()) continue;
            if (!isFullyLoaded(stand)) continue;
            if (!isNearOtherCorpse(stand)) continue;

            CorpseType type = getCorpseType(stand);
            if (type == CorpseType.None) continue;

            registerCorpse(new Corpse(stand, type));
        }
    }

    // -------------------------
    // CORE REGISTRATION
    // -------------------------

    private static void registerCorpse(Corpse corpse) {
        UUID uuid = corpse.getUuid();
        if (uuid == null) return;

        if (!knownCorpses.add(uuid)) return;

        corpses.add(corpse);

        eventBus.post(new CorpseEvent(corpse));
    }

    // -------------------------
    // DETECTION SAFETY CHECK
    // -------------------------

    private static boolean isFullyLoaded(ArmorStandEntity stand) {
        var armor = Utils.getEntityArmor(stand);
        ItemStack helmet = armor.getFirst();
        return !helmet.isEmpty();
    }

    // -------------------------
    // TYPE DETECTION
    // -------------------------

    public static CorpseType getCorpseType(ArmorStandEntity ent) {
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

    private static boolean isNearOtherCorpse(ArmorStandEntity stand) {
        for (Corpse corpse : corpses) {
            if (corpse.getEntity().squaredDistanceTo(stand) <= 9) {
                return true;
            }
        }

        return false;
    }

    // -------------------------
    // KEY CHECK
    // -------------------------

    public static boolean hasKeyForCorpse(CorpseType type) {
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
            if (!stack.isEmpty() && id.equals(Utils.getSkyblockId(stack))) {
                return true;
            }
        }

        return false;
    }

    // -------------------------
    // INTERACTION
    // -------------------------

    @EventHandler
    private void onInteractEntity(InteractEntityEvent event) {
        if (!(event.entity instanceof ArmorStandEntity stand)) return;

        CorpseType type = getCorpseType(stand);
        if (type == CorpseType.None) return;

        if (!hasKeyForCorpse(type)) return;

        for (Corpse corpse : corpses) {
            if (corpse.getEntity() == stand) {
                corpse.setOpened(true);
                break;
            }
        }
    }

    // -------------------------
    // JOIN RESET
    // -------------------------

    @EventHandler(priority = EventPriority.HIGH)
    private void onJoin(ServerJoinEvent event) {
        corpses.clear();
        knownCorpses.clear();

        if (mc.world == null || mc.player == null) return;

        if (Utils.isInArea(Area.MINESHAFT)) {
            updateCorpses();
        }
    }

    // -------------------------
    // MODEL
    // -------------------------

    public static class Corpse {
        private final ArmorStandEntity entity;
        private final CorpseType type;
        private boolean opened;

        public Corpse(ArmorStandEntity entity, CorpseType type) {
            this.entity = entity;
            this.type = type;
        }

        public ArmorStandEntity getEntity() {
            return entity;
        }

        public UUID getUuid() {
            return entity != null ? entity.getUuid() : null;
        }

        public CorpseType getType() {
            return type;
        }

        public boolean isOpened() {
            return opened;
        }

        public void setOpened(boolean opened) {
            this.opened = opened;
        }
    }

    public enum CorpseType {
        Lapis,
        Tungsten,
        Umber,
        Vanguard,
        None
    }
}