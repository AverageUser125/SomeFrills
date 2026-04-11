package com.somefrills.features.misc;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.somefrills.Main;
import com.somefrills.config.Feature;
import com.somefrills.config.FrillsConfig;
import com.somefrills.events.EntityUpdatedEvent;
import com.somefrills.events.ServerJoinEvent;
import com.somefrills.misc.EntityCache;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.JsonHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Entity highlighting feature that allows users to highlight entities based on name, type, or groups.
 * Groups are static (JSON-defined only) and can only be toggled on/off.
 */
public class GlowMob extends Feature {

    private static final ConcurrentHashMap<String, GlowMobRule> rules = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, RenderColor> activeGroups = new ConcurrentHashMap<>();
    // Armor stand cache: caches armor stands for stable mob associations
    private static final EntityCache armorStandCache = new EntityCache();
    // Search parameters for finding mobs near armor stands
    private static final double HORIZONTAL_RADIUS = 0.15;
    private static final double VERTICAL_RANGE = 4.0;
    private static ConcurrentHashMap<String, Set<GlowMobRule>> groups;

    public GlowMob() {
        super(FrillsConfig.instance.misc.glowMob.enabled);
    }

    private static synchronized void ensureGroupsLoaded() {
        if (groups != null) return;

        groups = new ConcurrentHashMap<>();

        try (InputStream inputStream = GlowMob.class.getResourceAsStream("/glowmob_groups.json")) {
            if (inputStream == null) {
                Main.LOGGER.debug("glowmob_groups.json not found, no static groups loaded.");
                return;
            }

            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                JsonObject root = new Gson().fromJson(reader, JsonObject.class);
                JsonObject groupsJson = JsonHelper.getObject(root, "groups");

                for (Map.Entry<String, JsonElement> entry : groupsJson.entrySet()) {
                    String groupName = normalizeGroupName(entry.getKey());
                    if (groupName == null) continue;

                    Set<GlowMobRule> groupRules = ConcurrentHashMap.newKeySet();

                    if (entry.getValue().isJsonArray()) {
                        for (JsonElement ruleElement : entry.getValue().getAsJsonArray()) {
                            if (ruleElement.isJsonObject()) {
                                JsonObject ruleObj = ruleElement.getAsJsonObject();
                                String name = JsonHelper.getString(ruleObj, "name", null);
                                String type = JsonHelper.getString(ruleObj, "type", null);
                                groupRules.add(new GlowMobRule(name, type, RenderColor.white));
                            }
                        }
                    }

                    groups.put(groupName, groupRules);
                }

                Main.LOGGER.debug("Loaded {} GlowMob groups.", groups.size());
            }
        } catch (IOException e) {
            Main.LOGGER.error("Failed to load glowmob_groups.json: {}", e.getMessage());
        }
    }

    /**
     * Determines if entity should glow and what color. Single pass through all rules.
     * Returns null if no match found.
     */
    private static GlowMatchResult findGlowMatch(Entity entity) {
        // Check direct rules first
        for (GlowMobRule rule : rules.values()) {
            if (rule.matchesEntity(entity)) {
                return new GlowMatchResult(rule.color, rule.name != null);
            }
        }

        // Then check active groups
        for (Map.Entry<String, RenderColor> entry : activeGroups.entrySet()) {
            for (GlowMobRule rule : getGroupRules(entry.getKey())) {
                if (rule.matchesEntity(entity)) {
                    return new GlowMatchResult(entry.getValue(), rule.name != null);
                }
            }
        }

        return null;
    }

    /**
     * If entity is a marker armor stand with a name rule match, find the actual mob to glow.
     * Otherwise, return the entity itself.
     */
    private static Entity resolveGlowTarget(Entity entity, boolean hasNameRule) {
        if (!(entity instanceof ArmorStandEntity armorStand) || !armorStand.isMarker() || !hasNameRule) {
            return entity;
        }

        // Check if we've cached this armor stand and if the associated mob still exists
        for (Entity cachedStand : armorStandCache.get()) {
            if (cachedStand.getId() == entity.getId()) {
                Entity closestMob = findClosestMobNearby(armorStand);
                if (closestMob != null) {
                    return closestMob;
                }
                break;
            }
        }

        // Search for closest mob nearby
        Entity closestMob = findClosestMobNearby(armorStand);
        if (closestMob != null) {
            armorStandCache.add(entity);
            return closestMob;
        }

        return entity;
    }

    private static Entity findClosestMobNearby(Entity armorStand) {
        double asX = armorStand.getX();
        double asY = armorStand.getY();
        double asZ = armorStand.getZ();

        Entity closest = null;
        double closestDist = Double.MAX_VALUE;

        for (Entity entity : Utils.getEntities()) {
            if (!(entity instanceof LivingEntity) || entity instanceof ArmorStandEntity) continue;

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
        Utils.getEntities().forEach(GlowMob::applyHighlight);
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
        for (var entity : Utils.getEntities()) {
            if (entity instanceof PlayerEntity) continue;
            Utils.setGlowing(entity, false, RenderColor.white);
        }
        rules.clear();
    }

    private static void clearGlowForRule(GlowMobRule rule) {
        for (Entity entity : Utils.getEntities()) {
            if (rule.matchesEntity(entity)) {
                // Resolve the actual entity that should be un-glowed
                Entity targetEntity = resolveGlowTarget(entity, rule.name != null);
                Utils.setGlowing(targetEntity, false, RenderColor.white);
            }
        }
    }

    public static boolean addGroup(String group, RenderColor color) {
        ensureGroupsLoaded();

        String normalized = normalizeGroupName(group);
        if (normalized == null || !groups.containsKey(normalized)) {
            return false;
        }

        activeGroups.put(normalized, color);
        reapplyHighlights();
        return true;
    }

    public static Collection<GlowMobRule> getRules() {
        return List.copyOf(rules.values());
    }

    public static boolean removeGroup(String group) {
        String normalized = normalizeGroupName(group);
        if (normalized == null) return false;

        if (activeGroups.remove(normalized) == null) {
            return false;
        }

        clearGlowForGroup(normalized);
        return true;
    }

    private static void clearGlowForGroup(String groupName) {
        for (Entity entity : Utils.getEntities()) {
            for (GlowMobRule rule : getGroupRules(groupName)) {
                if (rule.matchesEntity(entity)) {
                    // Resolve the actual entity that should be un-glowed
                    Entity targetEntity = resolveGlowTarget(entity, rule.name != null);
                    Utils.setGlowing(targetEntity, false, RenderColor.white);
                    break;
                }
            }
        }
    }

    private static String generateRuleKey(String name, String type) {
        String normalizedName = normalizeRuleName(name);
        String normalizedType = (type == null || type.isEmpty()) ? "ANY" : type.toLowerCase();
        return (normalizedName == null ? "ANY" : normalizedName) + ":" + normalizedType;
    }

    public static Collection<GlowMobRule> getGroupRules(String group) {
        ensureGroupsLoaded();

        String normalized = normalizeGroupName(group);
        if (normalized == null || groups == null) return List.of();

        Set<GlowMobRule> set = groups.get(normalized);
        return set == null ? List.of() : List.copyOf(set);
    }

    public static boolean groupExists(String group) {
        ensureGroupsLoaded();
        String normalized = normalizeGroupName(group);
        return normalized != null && groups.containsKey(normalized);
    }

    public static Collection<String> getGroupNames() {
        ensureGroupsLoaded();
        return groups == null ? List.of() : List.copyOf(groups.keySet());
    }

    public static Collection<String> getActiveGroupNames() {
        return List.copyOf(activeGroups.keySet());
    }

    public static ConcurrentHashMap<String, RenderColor> getActiveGroups() {
        return activeGroups;
    }

    private static String normalizeRuleName(String name) {
        if (name == null) return null;

        String normalized = name.trim().toLowerCase();
        if (normalized.isEmpty()) return null;

        if (normalized.startsWith("#")) {
            return "#" + normalizeGroupName(normalized);
        }

        return normalized;
    }

    private static String normalizeGroupName(String group) {
        if (group == null) return null;

        String normalized = group.trim();
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }

        normalized = normalized.trim().toLowerCase();
        return normalized.isEmpty() ? null : normalized;
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onEntityUpdate(EntityUpdatedEvent event) {
        if (!isActive()) return;
        applyHighlight(event.entity);
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onServerJoin(ServerJoinEvent ignored) {
        for (Entity entity : Utils.getEntities()) {
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
         * Check if this rule matches an entity, including group references
         */
        public boolean matches(Entity entity) {
            if (this.name != null && this.name.startsWith("#")) {
                // This rule references a group
                Collection<GlowMobRule> groupRules = GlowMob.getGroupRules(this.name);
                for (GlowMobRule rule : groupRules) {
                    if (rule.matchesEntity(entity)) {
                        return true;
                    }
                }
                return false;
            }

            return matchesEntity(entity);
        }

        /**
         * Check if this rule matches an entity (direct match only, no group references)
         * <p>
         * Rules are applied as follows:
         * - If name is specified: only armor stands can match (by name). The nearby mob will be resolved later.
         * - If name is not specified: match any entity of the specified type.
         */
        boolean matchesEntity(Entity entity) {
            // If a name is specified, only armor stands with that name can trigger this rule
            if (this.name != null) {
                if (!(entity instanceof ArmorStandEntity)) {
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