package com.somefrills.features.misc.glowmob;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.somefrills.misc.Area;
import com.somefrills.misc.SortedList;
import com.somefrills.misc.Utils;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class MatchInfo {
    private static final double HORIZONTAL_RADIUS = 0.25;
    private static final double VERTICAL_RANGE = 4.0;

    @NonNull
    public SortedList<String> type;
    @NonNull
    public String name;
    @Nullable
    public Area area;
    @NonNull
    public Set<GearFlag> gear;
    public int maxHp;

    public MatchInfo(MatchInfo info) {
        this.type = new SortedList<>(info.type);
        this.name = info.name;
        this.area = info.area;
        this.gear = EnumSet.copyOf(info.gear);
        this.maxHp = 0;
    }

    public MatchInfo(@NonNull List<String> type, @NonNull String name, @Nullable Area area, @NonNull Set<GearFlag> gear, int maxHp) {
        this.type = new SortedList<>(type);
        this.name = name;
        this.area = area;
        this.gear = gear;
        this.maxHp = maxHp;
    }

    public MatchInfo() {
        this.type = new SortedList<>();
        this.name = "";
        this.area = null;
        this.gear = EnumSet.noneOf(GearFlag.class);
        this.maxHp = 0;
    }

    public boolean isEmpty() {
        return type.isEmpty() && name.trim().isEmpty() && area == null && gear.isEmpty() && maxHp <= 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MatchInfo other)) return false;
        return type.equals(other.type)
                && name.equals(other.name)
                && ((area == null && other.area == null) || (area != null && area.equals(other.area)))
                && gear.equals(other.gear);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (area != null ? area.hashCode() : 0);
        result = 31 * result + gear.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return serialize();
    }

    public void clear() {
        this.type = new SortedList<>();
        this.name = "";
        this.area = null;
        this.gear.clear();
    }

    public Predicate<LivingEntity> compile() {
        Predicate<LivingEntity> predicate = e -> true;
        if (area != null) {
            predicate = predicate.and(new AreaPredicate(area));
        }
        if (!type.isEmpty()) {
            predicate = predicate.and(new MultiTypePredicate(type));
        }
        if (!name.trim().isEmpty()) {
            predicate = predicate.and(new NamePredicate(name));
        }
        if (!gear.isEmpty()) {
            if (gear.contains(GearFlag.NAKED)) {
                predicate = predicate.and(new NakedPredicate());
            } else {
                predicate = predicate.and(new GearPredicate(gear));
            }
        }
        if (maxHp > 0) {
            predicate = predicate.and(new MaxHpPredicate(maxHp));
        }
        return predicate;
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();

        if (!type.isEmpty()) {
            JsonArray types = new JsonArray();
            for (String t : type) {
                types.add(t);
            }
            obj.add("type", types);
        }

        if (!name.isEmpty()) {
            obj.addProperty("name", name);
        }

        if (area != null) {
            obj.addProperty("area", area.getDisplayName());
        }

        if (!gear.isEmpty()) {
            JsonArray gears = new JsonArray();
            for (GearFlag g : gear) {
                gears.add(g.name());
            }
            obj.add("gear", gears);
        }

        if (maxHp > 0) {
            obj.addProperty("maxHp", maxHp);
        }

        return obj;
    }

    public String serialize() {
        return toJson().toString();
    }

    public static MatchInfo fromString(String str) throws MatcherParseException {
        if (str == null || str.trim().isEmpty()) {
            throw new MatcherParseException("Empty matcher expression");
        }
        try {
            JsonObject obj = JsonParser.parseString(str).getAsJsonObject();
            return fromJson(obj);
        } catch (JsonSyntaxException | IllegalStateException e) {
            throw new MatcherParseException("Invalid JSON format: " + e.getMessage());
        }
    }

    public static MatchInfo fromJson(JsonObject obj) throws MatcherParseException {
        MatchInfo info = new MatchInfo();

        if (obj.has("type")) {
            info.type = new SortedList<>();
            for (JsonElement el : obj.getAsJsonArray("type")) {
                info.type.add(el.getAsString());
            }
        }

        if (obj.has("name")) {
            info.name = obj.get("name").getAsString();
        }

        if (obj.has("area")) {
            info.area = Area.fromString(obj.get("area").getAsString());
        }

        if (obj.has("gear")) {
            info.gear = EnumSet.noneOf(GearFlag.class);

            for (JsonElement el : obj.getAsJsonArray("gear")) {
                try {
                    info.gear.add(GearFlag.valueOf(el.getAsString().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    throw new MatcherParseException("Unknown gear: " + el.getAsString());
                }
            }
        }

        if (obj.has("maxHp")) {
            try {
                info.maxHp = obj.get("maxHp").getAsInt();
            } catch (NumberFormatException e) {
                throw new MatcherParseException("Invalid maxHp value");
            }
        }

        return info;
    }

    public enum GearFlag {
        NAKED,
        CHEST,
        LEGS,
        FEET,
        HEAD
    }

    public static class MatchInfoTypeAdapter extends TypeAdapter<MatchInfo> {
        private final Gson gson = new Gson();
        @Override
        public void write(JsonWriter out, MatchInfo value) {
            gson.toJson(value.toJson(), out);
        }

        @Override
        public MatchInfo read(JsonReader in) {
            JsonObject obj = JsonParser.parseReader(in).getAsJsonObject();
            try {
                return MatchInfo.fromJson(obj);
            } catch (MatcherParseException e) {
                throw new JsonParseException("Failed to parse MatchInfo: " + e.getMessage(), e);
            }
        }
    }

    public static class AreaPredicate implements Predicate<LivingEntity> {
        private final Area area;

        public AreaPredicate(Area area) {
            this.area = area;
        }

        @Override
        public boolean test(LivingEntity entity) {
            return Utils.isInArea(area);
        }
    }

    private static class NamePredicate implements Predicate<LivingEntity> {
        private final String name;

        public NamePredicate(String name) {
            this.name = name.toLowerCase();
        }

        private static List<LivingEntity> getNearbyEntities(LivingEntity entity) {
            var world = entity.getEntityWorld();
            if (world == null) {
                return Collections.emptyList();
            }

            var box = entity.getBoundingBox()
                    .expand(HORIZONTAL_RADIUS * 2, VERTICAL_RANGE, HORIZONTAL_RADIUS * 2);

            return world.getEntitiesByClass(ArmorStandEntity.class, box, e -> true)
                    .stream()
                    .map(e -> (LivingEntity) e)
                    .toList();
        }

        @Override
        public boolean test(LivingEntity entity) {
            return hasNamedArmorStandAbove(entity);
        }

        private boolean hasNamedArmorStandAbove(LivingEntity entity) {
            double eX = entity.getX();
            double eY = entity.getY();
            double eZ = entity.getZ();

            for (LivingEntity nearby : getNearbyEntities(entity)) {
                if (!(nearby instanceof ArmorStandEntity armorStand)) {
                    continue;
                }

                double asY = armorStand.getY();
                if (asY < eY || asY - eY >= VERTICAL_RANGE) {
                    continue;
                }

                double dx = armorStand.getX() - eX;
                double dz = armorStand.getZ() - eZ;
                double horizontalDist = Math.hypot(dx, dz);

                if (horizontalDist <= HORIZONTAL_RADIUS) {
                    String asName = Utils.toPlain(armorStand.getDisplayName()).toLowerCase();
                    if (asName.contains(this.name)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public static class GearPredicate implements Predicate<LivingEntity> {
        private final Set<GearFlag> requiredGear;

        public GearPredicate(Set<GearFlag> requiredGear) {
            this.requiredGear = requiredGear;
        }

        @Override
        public boolean test(LivingEntity entity) {
            return requiredGear.contains(GearFlag.CHEST) && !entity.getEquippedStack(EquipmentSlot.CHEST).isEmpty()
                    || requiredGear.contains(GearFlag.LEGS) && !entity.getEquippedStack(EquipmentSlot.LEGS).isEmpty()
                    || requiredGear.contains(GearFlag.FEET) && !entity.getEquippedStack(EquipmentSlot.FEET).isEmpty()
                    || requiredGear.contains(GearFlag.HEAD) && !entity.getEquippedStack(EquipmentSlot.HEAD).isEmpty();
        }
    }

    public static class NakedPredicate implements Predicate<LivingEntity> {
        public NakedPredicate() {
        }

        @Override
        public boolean test(LivingEntity entity) {
            // FIXME: hurtTime is not good, as it means the mob may flicker, but it should still work
            // Without this checks, mobs that are dying or recently spawned will be considered naked, which is not ideal
            if (entity.isDead() || entity.age <= 2 || entity.deathTime > 0 || entity.hurtTime > 0) {
                return false;
            }

            return Utils.isNaked(entity);
        }
    }

    public static class MaxHpPredicate implements Predicate<LivingEntity> {
        private final int maxHp;

        public MaxHpPredicate(int maxHp) {
            this.maxHp = maxHp;
        }

        @Override
        public boolean test(LivingEntity entity) {
            return entity.getMaxHealth() == maxHp;
        }
    }

    public static class MatcherParseException extends Exception {
        @Serial
        private static final long serialVersionUID = 1L;

        public MatcherParseException(String message) {
            super(message);
        }
    }

    public static class MultiTypePredicate implements Predicate<LivingEntity> {
        private final List<String> entityTypes;
        private static final int PREFIX_LENGTH = "entity.minecraft.".length();

        public MultiTypePredicate(List<String> entityTypes) {
            this.entityTypes = entityTypes;
        }

        @Override
        public boolean test(LivingEntity entity) {
            String entityTypeStr = entity.getType().toString().toLowerCase();
            return entityTypes.stream().anyMatch(s -> specializedEquals(entityTypeStr, s));
        }

        public static boolean specializedEquals(String entityTypeStr, String entityType) {
            if (entityTypeStr.length() - PREFIX_LENGTH != entityType.length()) {
                return false;
            }
            for (int i = 0; i < entityType.length(); i++) {
                char c1 = entityTypeStr.charAt(i + PREFIX_LENGTH);
                char c2 = entityType.charAt(i);
                if (c1 != c2) {
                    return false;
                }
            }
            return true;
        }
    }
}
