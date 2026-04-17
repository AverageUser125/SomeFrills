package com.somefrills.features.misc.matcher;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.somefrills.misc.Area;
import com.somefrills.misc.Utils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
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
    public String type;
    @NonNull
    public String name;
    @Nullable
    public Area area;
    @NonNull
    public Set<GearFlag> gear;

    public MatchInfo(MatchInfo info) {
        this.type = info.type;
        this.name = info.name;
        this.area = info.area;
        this.gear = EnumSet.copyOf(info.gear);
    }

    public boolean isEmpty() {
        return type.trim().isEmpty() && name.trim().isEmpty() && area == null && gear.isEmpty();
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
        this.type = "";
        this.name = "";
        this.area = null;
        this.gear.clear();
    }

    public static class MatchInfoTypeAdapter extends TypeAdapter<MatchInfo> {
        @Override
        public void write(JsonWriter out, MatchInfo value) throws IOException {
            out.value(value.serialize());
        }

        @Override
        public MatchInfo read(JsonReader in) throws IOException {
            String str = in.nextString();
            try {
                return fromString(str);
            } catch (MatcherParseException e) {
                throw new JsonParseException("Failed to parse MatchInfo: " + e.getMessage(), e);
            }
        }
    }

    public enum GearFlag {
        NAKED,
        CHEST,
        LEGS,
        FEET,
        HEAD
    }

    public Predicate<LivingEntity> compile() {
        Predicate<LivingEntity> predicate = e -> true;
        if(area != null) {
            predicate = predicate.and(new AreaPredicate(area));
        }
        if(!type.trim().isEmpty()) {
            predicate = predicate.and(new TypePredicate(type));
        }
        if(!name.trim().isEmpty()) {
            predicate = predicate.and(new NamePredicate(name));
        }
        if(!gear.isEmpty()) {
            predicate = predicate.and(new GearPredicate(gear));
        }
        return predicate;
    }

    public static MatchInfo fromString(String str) throws MatcherParseException {
        if (str == null || str.trim().isEmpty()) {
            throw new MatcherParseException("Empty matcher expression");
        }

        MatchInfo info = new MatchInfo();
        String[] pairs = str.split(",");

        for (String pair : pairs) {
            pair = pair.trim();
            if (pair.isEmpty()) continue;

            int eqIndex = pair.indexOf('=');
            if (eqIndex == -1) continue;

            String key = pair.substring(0, eqIndex).trim().toUpperCase();
            String value = pair.substring(eqIndex + 1).trim();

            // Handle quoted strings
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }

            if(value.isEmpty()) {
                continue;
            }

            switch (key) {
                case "TYPE" -> info.type = value;
                case "NAME" -> info.name = value;
                case "AREA" -> {
                    info.area = Area.fromString(value);
                }
                case "GEAR" -> {
                    info.gear = EnumSet.noneOf(GearFlag.class);
                    String[] gearValues = value.split("\\+");
                    for (String gearValue : gearValues) {
                        try {
                            info.gear.add(GearFlag.valueOf(gearValue.trim().toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            throw new MatcherParseException("Unknown gear: " + gearValue);
                        }
                    }
                }
                default -> throw new MatcherParseException("Unknown key: " + key);
            }
        }

        return info;
    }

    public MatchInfo(@NonNull String type, @NonNull String name, @Nullable Area area, @NonNull Set<GearFlag> gear) {
        this.type = type;
        this.name = name;
        this.area = area;
        this.gear = gear;
    }

    public MatchInfo() {
        this.type = "";
        this.name = "";
        this.area = null;
        this.gear = EnumSet.noneOf(GearFlag.class);
    }

    public String serialize() {
        List<String> parts = new java.util.ArrayList<>();

        if (!type.isEmpty()) {
            parts.add("TYPE=" + type);
        }

        if (!name.isEmpty()) {
            if (name.contains(",") || name.contains(" ")) {
                parts.add("NAME=\"" + name + "\"");
            } else {
                parts.add("NAME=" + name);
            }
        }

        if (area != null) {
            parts.add("AREA=" + area.getDisplayName());
        }

        if (!gear.isEmpty()) {
            String gearStr = String.join("+", gear.stream().map(Enum::name).toList());
            parts.add("GEAR=" + gearStr);
        }

        return String.join(",", parts);
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

    public static class TypePredicate implements Predicate<LivingEntity> {
        private final String entityType;
        private static final int PREFIX_LENGTH = "entity.minecraft.".length();

        public TypePredicate(String entityType) {
            this.entityType = Utils.stripPrefix(entityType, "minecraft:").toLowerCase();
        }

        @Override
        public boolean test(LivingEntity entity) {
            String entityTypeStr = entity.getType().toString();
            if(entityTypeStr.length() - PREFIX_LENGTH != this.entityType.length()) {
                return false;
            }
            for(int i = 0; i < this.entityType.length(); i++) {
                char c1 = entityTypeStr.charAt(i + PREFIX_LENGTH);
                char c2 = this.entityType.charAt(i);
                if(c1 != c2) {
                    return false;
                }
            }
            return true;
        }
    }

    private static class NamePredicate implements Predicate<LivingEntity> {
        private final String name;

        public NamePredicate(String name) {
            this.name = name.toLowerCase();
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
    }

    public static class GearPredicate implements Predicate<LivingEntity> {
        private final Set<GearFlag> requiredGear;

        public GearPredicate(Set<GearFlag> requiredGear) {
            this.requiredGear = requiredGear;
        }

        @Override
        public boolean test(LivingEntity entity) {
            if (requiredGear.contains(GearFlag.NAKED)) {
                return isNaked(entity);
            }

            return requiredGear.contains(GearFlag.CHEST) && !entity.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST).isEmpty()
                    || requiredGear.contains(GearFlag.LEGS) && !entity.getEquippedStack(net.minecraft.entity.EquipmentSlot.LEGS).isEmpty()
                    || requiredGear.contains(GearFlag.FEET) && !entity.getEquippedStack(net.minecraft.entity.EquipmentSlot.FEET).isEmpty()
                    || requiredGear.contains(GearFlag.HEAD) && !entity.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD).isEmpty();
        }

        private boolean isNaked(LivingEntity entity) {
            return entity.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST).isEmpty()
                    && entity.getEquippedStack(net.minecraft.entity.EquipmentSlot.LEGS).isEmpty()
                    && entity.getEquippedStack(net.minecraft.entity.EquipmentSlot.FEET).isEmpty()
                    && entity.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD).isEmpty();
        }
    }

    public static class MatcherParseException extends Exception {
        @Serial
        private static final long serialVersionUID = 1L;
        public MatcherParseException(String message) {
            super(message);
        }
    }
}