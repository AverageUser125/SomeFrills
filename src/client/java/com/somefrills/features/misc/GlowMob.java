package com.somefrills.features.misc;

import com.somefrills.config.Feature;
import com.somefrills.config.FrillsConfig;
import com.somefrills.events.WorldTickEvent;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.somefrills.Main.mc;

/**
 * Entity highlighting feature that allows users to highlight entities based on name or type.
 */
public class GlowMob extends Feature {

    private static final double HORIZONTAL_RADIUS = 0.5;
    private static final double VERTICAL_RANGE = 4.0;

    private final ConcurrentHashMap<String, GlowMobRule> rules = new ConcurrentHashMap<>();
    private List<Entity> entityList;

    public GlowMob() {
        super(FrillsConfig.instance.misc.glowMob.enabled);
    }

    /**
     * Determines if entity should glow and what color. Single pass through all rules.
     * Returns null if no match found.
     */
    private GlowMatchResult findGlowMatch(Entity entity) {
        for (GlowMobRule rule : rules.values()) {
            if (rule.matchesEntity(entity)) {
                return new GlowMatchResult(rule.color, rule.name != null);
            }
        }
        return null;
    }

    /**
     * If entity is an armor stand with a name rule match, find the actual mob to glow.
     * If entity is a regular living entity with the name on it, return it as-is.
     * Otherwise, return the entity itself.
     * Note: This handles both marker and non-marker armor stands, as newly-appearing armor stands
     * may not yet be marked as markers when they're first updated.
     */
    private Entity resolveGlowTarget(Entity entity, boolean hasNameRule) {
        // If it's not a name-based match, return as-is
        if (!hasNameRule) {
            return entity;
        }

        // If it's a regular living entity (not an armor stand) with a name, glow it directly
        if (entity instanceof LivingEntity && !(entity instanceof ArmorStandEntity)) {
            return entity;
        }

        // If it's an armor stand with a name rule, try to find the actual mob nearby
        if (entity instanceof ArmorStandEntity armorStand) {
            Entity closestMob = findClosestMobNearby(armorStand);
            if (closestMob != null) {
                return closestMob;
            }
            // If no mob found nearby, glow the armor stand itself
            return entity;
        }

        return entity;
    }

    private Entity findClosestMobNearby(Entity armorStand) {
        double asX = armorStand.getX();
        double asY = armorStand.getY();
        double asZ = armorStand.getZ();


        if (mc.world == null) return null;

        // Search all entities and find the first one that matches criteria
        for (Entity entity : getEntities()) {

            double entityY = entity.getY();
            // Entity must be at or below the armor stand
            if (entityY > asY || asY - entityY >= VERTICAL_RANGE) {

                continue;
            }

            // Check horizontal distance
            double dx = entity.getX() - asX;
            double dz = entity.getZ() - asZ;
            double horizontalDist = Math.hypot(dx, dz);

            if (horizontalDist <= HORIZONTAL_RADIUS) {

                return entity;
            }
        }


        return null;
    }


    private void applyHighlight(Entity entity) {
        if (!(entity instanceof LivingEntity)) return;

        GlowMatchResult result = findGlowMatch(entity);
        if (result != null) {

            Entity targetEntity = resolveGlowTarget(entity, result.hasNameRule);

            Utils.setGlowing(targetEntity, true, result.color);
        } else {

        }
    }

    public boolean addRule(String name, String type, RenderColor color) {
        String key = generateRuleKey(name, type);
        return rules.put(key, new GlowMobRule(name, type, color)) == null; // Rule already existed
    }

    public boolean removeRule(String name, String type) {
        String key = generateRuleKey(name, type);
        GlowMobRule removed = rules.remove(key);
        if (removed != null) {
            // Disable glowing on entities that matched this rule
            for (Entity entity : getEntities()) {
                if (removed.matchesEntity(entity)) {
                    Entity targetEntity = resolveGlowTarget(entity, removed.name != null);
                    Utils.setGlowing(targetEntity, false, RenderColor.white);
                }
            }
        }
        return removed != null;
    }

    public void clearRules() {
        // Disable glowing on all entities that match any rule before clearing
        for (Entity entity : getEntities()) {
            GlowMatchResult result = findGlowMatch(entity);
            if (result != null) {
                Entity targetEntity = resolveGlowTarget(entity, result.hasNameRule);
                Utils.setGlowing(targetEntity, false, RenderColor.white);
            }
        }
        rules.clear();
    }

    public Collection<GlowMobRule> getRules() {
        return List.copyOf(rules.values());
    }

    private static String generateRuleKey(String name, String type) {
        String normalizedName = normalizeRuleName(name);
        String normalizedType = (type == null || type.isEmpty()) ? "ANY" : type.toLowerCase();
        return (normalizedName == null ? "ANY" : normalizedName) + ":" + normalizedType;
    }

    private static String normalizeRuleName(String name) {
        if (name == null) return null;

        String normalized = name.trim().toLowerCase();
        if (normalized.isEmpty()) return null;

        return normalized;
    }

    private List<Entity> getEntities() {
        if (entityList == null) {
            entityList = updateEntities();
        }
        return entityList;
    }

    private static List<Entity> updateEntities() {
        return Utils.getEntities().stream()
                .filter(Utils::isMob)
                .toList();
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onWorldTick(WorldTickEvent event) {
        if (!isActive()) return;
        entityList = updateEntities();
        getEntities().forEach(this::applyHighlight);
    }

    /**
     * Result of glow matching: color and whether it matched a name-based rule
     */
    private record GlowMatchResult(RenderColor color, boolean hasNameRule) {
    }

    public record GlowMobRule(String name, String type, RenderColor color) {
        public GlowMobRule(String name, String type, RenderColor color) {
            this.name = normalizeRuleName(name);
            this.type = normalizeType(type);
            this.color = color;
        }

        private static String normalizeType(String type) {
            if (type == null || type.isEmpty()) {
                return null;
            }
            return Utils.stripPrefix(type, "minecraft:").toLowerCase();
        }

        /**
         * Check if this rule matches an entity (direct match only)
         * <p>
         * Rules are applied as follows:
         * - If name is specified: match any living entity with that name (armor stands or direct mobs).
         * - If name is not specified: match any entity of the specified type.
         */
        boolean matchesEntity(Entity entity) {
            // If a name is specified, match any living entity with that name
            if (this.name != null) {
                if (!(entity instanceof LivingEntity)) {
                    return false;
                }
                String entityName = Utils.toPlain(entity.getDisplayName()).toLowerCase();
                return entityName.contains(this.name);
            }

            // No name specified: match by type if specified
            if (this.type != null) {
                String entityType = entity.getType().toString();
                entityType = Utils.stripPrefix(entityType, "entity.minecraft.").toLowerCase();
                return entityType.equals(this.type);
            }

            // No criteria = match everything
            return true;
        }
    }
}
