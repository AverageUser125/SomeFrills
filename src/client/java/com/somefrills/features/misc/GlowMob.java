package com.somefrills.features.misc;

import com.somefrills.config.Feature;
import com.somefrills.config.FrillsConfig;
import com.somefrills.events.EntityUpdatedEvent;
import com.somefrills.events.ServerJoinEvent;
import com.somefrills.misc.EntityCache;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.somefrills.Main.mc;

/**
 * Entity highlighting feature that allows users to highlight entities based on name or type.
 */
public class GlowMob extends Feature {

    private static final ConcurrentHashMap<String, GlowMobRule> rules = new ConcurrentHashMap<>();
    // Armor stand cache: caches armor stands for stable mob associations
    private static final EntityCache armorStandCache = new EntityCache();
    // Search parameters for finding mobs near armor stands
    private static final double HORIZONTAL_RADIUS = 0.15;
    private static final double VERTICAL_RANGE = 4.0;

    public GlowMob() {
        super(FrillsConfig.instance.misc.glowMob.enabled);
    }

    /**
     * Determines if entity should glow and what color. Single pass through all rules.
     * Returns null if no match found.
     */
    private static GlowMatchResult findGlowMatch(Entity entity) {
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
    private static Entity resolveGlowTarget(Entity entity, boolean hasNameRule) {
        // If it's not a name-based match, return as-is
        if (!hasNameRule) {
            return entity;
        }

        // If it's a regular living entity (not an armor stand) with a name, glow it directly
        if (entity instanceof LivingEntity && !(entity instanceof ArmorStandEntity)) {
            return entity;
        }

        // If it's an armor stand (marker or not), try to find the actual mob nearby
        if (entity instanceof ArmorStandEntity armorStand) {
            Entity closestMob = findClosestMobNearby(armorStand);
            if (closestMob != null) {
                armorStandCache.add(entity);
                return closestMob;
            }
        }

        return entity;
    }

    private static Entity findClosestMobNearby(Entity armorStand) {
        double asX = armorStand.getX();
        double asY = armorStand.getY();
        double asZ = armorStand.getZ();

        Entity closest = null;
        double closestDist = Double.MAX_VALUE;

        // Search in both getEntities() and mc.world to catch entities that might not be synced yet
        Set<Entity> entitiesToSearch = new HashSet<>(getEntities());
        if (mc.world != null) {
            for (Entity e : mc.world.getEntities()) {
                entitiesToSearch.add(e);
            }
        }

        for (Entity entity : entitiesToSearch) {
            if (!(entity instanceof LivingEntity) || entity instanceof ArmorStandEntity) continue;

            // Skip real players
            if (entity instanceof PlayerEntity && Utils.isPlayer((PlayerEntity) entity)) continue;

            double entityY = entity.getY();

            // Entity must be at or below the armor stand
            if (entityY > asY || asY - entityY > VERTICAL_RANGE) continue;

            // Check horizontal distance
            double dx = entity.getX() - asX;
            double dz = entity.getZ() - asZ;
            double horizontalDist = Math.sqrt(dx * dx + dz * dz);
            if (horizontalDist > HORIZONTAL_RADIUS) continue;

            double dist = entity.distanceTo(armorStand);
            if (dist < closestDist) {
                closestDist = dist;
                closest = entity;
            }
        }

        return closest;
    }


    private static void applyHighlight(Entity entity) {
        if (!(entity instanceof LivingEntity)) return;

        GlowMatchResult result = findGlowMatch(entity);
        if (result != null) {
            Entity targetEntity = resolveGlowTarget(entity, result.hasNameRule);
            Utils.setGlowing(targetEntity, true, result.color);
        }
    }

    private static void reapplyHighlights() {
        getEntities().forEach(GlowMob::applyHighlight);
    }

    public static boolean addRule(String name, String type, RenderColor color) {
        String key = generateRuleKey(name, type);
        if (rules.put(key, new GlowMobRule(name, type, color)) != null) {
            return false; // Rule already existed
        }
        reapplyHighlights();
        return true;
    }

    public static boolean removeRule(String name, String type) {
        String key = generateRuleKey(name, type);
        GlowMobRule removed = rules.remove(key);
        if (removed == null) return false;

        clearGlowForRule(removed);
        return true;
    }

    public static void clearRules() {
        for (var entity : getEntities()) {
            if (entity instanceof PlayerEntity) continue;
            Utils.setGlowing(entity, false, RenderColor.white);
        }
        rules.clear();
    }

    private static void clearGlowForRule(GlowMobRule rule) {
        // Clear all glow from non-player entities to ensure complete removal
        Set<Entity> entitiesToClear = new HashSet<>(getEntities());
        if (mc.world != null) {
            for (Entity e : mc.world.getEntities()) {
                entitiesToClear.add(e);
            }
        }

        for (Entity entity : entitiesToClear) {
            if (entity instanceof PlayerEntity) continue;
            Utils.setGlowing(entity, false, RenderColor.white);
        }

        // Reapply highlights for all remaining rules
        reapplyHighlights();
    }

    public static Collection<GlowMobRule> getRules() {
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


    private static List<Entity> getEntities(){
        var list = Utils.getEntities();
        if(mc.world == null) return list;
        ArrayList<Entity> entities = new ArrayList<>(list);
        for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
            var uuid = player.getUuid();
            if(uuid == null) {
                entities.add(player);
                continue;
            }
            if (uuid.version() != 4) entities.add(player);
        }
        return entities;
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onEntityUpdate(EntityUpdatedEvent event) {
        if (!isActive()) return;
        applyHighlight(event.entity);
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onServerJoin(ServerJoinEvent ignored) {
        for (Entity entity : getEntities()) {
            applyHighlight(entity);
        }
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