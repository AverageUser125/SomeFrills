package com.somefrills.features.misc.matcher;

import com.somefrills.misc.Utils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static com.somefrills.features.misc.matcher.MatcherTypes.NAME;

/**
 * Matches entities by their display name.
 * For regular entities: checks if the entity has the name OR if there's an armor stand above it with the name.
 * Example: NAME=Littlefoot
 * Priority: 1 (minimum - evaluated last, as this is an expensive check)
 */
public class NameMatcher implements Matcher {
    private static final double HORIZONTAL_RADIUS = 0.25;
    private static final double VERTICAL_RANGE = 4.0;

    private final String name;

    public NameMatcher(String name) {
        this.name = name.trim().toLowerCase();
    }

    @Override
    public Predicate<LivingEntity> compile() {
        return entity -> {
            // First check if the entity itself has the name
            String entityName = Utils.toPlain(entity.getDisplayName()).toLowerCase();
            if (entityName.contains(this.name)) {
                return true;
            }

            // For regular entities, check if there's an armor stand above with the name
            if (!(entity instanceof ArmorStandEntity)) {
                return hasNamedArmorStandAbove(entity);
            }

            return false;
        };
    }

    /**
     * Check if there's a nearby armor stand above this entity with the matching name.
     */
    private boolean hasNamedArmorStandAbove(LivingEntity entity) {
        double eX = entity.getX();
        double eY = entity.getY();
        double eZ = entity.getZ();

        for (LivingEntity nearby : getNearbyEntities(entity)) {
            if (!(nearby instanceof ArmorStandEntity armorStand)) {
                continue;
            }

            double asY = armorStand.getY();
            // Armor stand must be at or above the entity
            if (asY < eY || asY - eY >= VERTICAL_RANGE) {
                continue;
            }

            // Check horizontal distance
            double dx = armorStand.getX() - eX;
            double dz = armorStand.getZ() - eZ;
            double horizontalDist = Math.hypot(dx, dz);

            if (horizontalDist <= HORIZONTAL_RADIUS) {
                // Check if the armor stand has the matching name
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

        // ArmorStandEntity::isMarker Removed for debugging
        return world.getEntitiesByClass(ArmorStandEntity.class, box, e -> true)
                .stream()
                .map(e -> (LivingEntity) e)
                .toList();
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public String toString() {
        return NAME + "=" + name;
    }
}


