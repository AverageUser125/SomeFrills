package com.somefrills.features.misc;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.somefrills.Main;
import com.somefrills.config.Feature;
import com.somefrills.config.FrillsConfig;
import com.somefrills.events.EntityUpdatedEvent;
import com.somefrills.events.ServerJoinEvent;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
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

    // Active (enabled) groups with their color
    private static final ConcurrentHashMap<String, RenderColor> activeGroups = new ConcurrentHashMap<>();
    // Static groups loaded from JSON (read-only)
    private static ConcurrentHashMap<String, Set<GlowMobRule>> groups = null;

    public GlowMob() {
        super(FrillsConfig.instance.misc.glowMob.enabled);
    }

    private static void ensureGroupsLoaded() {
        if (groups != null) return;

        groups = new ConcurrentHashMap<>();

        try (InputStream inputStream = GlowMob.class.getResourceAsStream("/glowmob_groups.json")) {
            if (inputStream == null) {
                Main.LOGGER.debug("glowmob_groups.json not found, no static groups loaded.");
                return;
            }

            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                Gson gson = new Gson();
                JsonObject root = gson.fromJson(reader, JsonObject.class);
                JsonObject groupsJson = JsonHelper.getObject(root, "groups");

                for (Map.Entry<String, JsonElement> entry : groupsJson.entrySet()) {
                    String groupName = normalizeGroupName(entry.getKey());
                    Set<GlowMobRule> groupRules = ConcurrentHashMap.newKeySet();

                    if (entry.getValue().isJsonArray()) {
                        for (JsonElement ruleElement : entry.getValue().getAsJsonArray()) {
                            if (!ruleElement.isJsonObject()) continue;

                            JsonObject ruleObj = ruleElement.getAsJsonObject();
                            String name = JsonHelper.getString(ruleObj, "name", null);
                            String type = JsonHelper.getString(ruleObj, "type", null);

                            groupRules.add(new GlowMobRule(name, type, RenderColor.white));
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

    private static void applyHighlight(Entity entity) {
        if (!(entity instanceof LivingEntity)) return;

        // 1. Direct rules (highest priority)
        for (GlowMobRule rule : rules.values()) {
            if (!rule.matches(entity)) continue;

            Utils.setGlowing(entity, true, rule.color);
            return;
        }

        // 2. Active groups
        for (Map.Entry<String, RenderColor> entry : activeGroups.entrySet()) {
            String group = entry.getKey();
            RenderColor color = entry.getValue();

            for (GlowMobRule rule : getGroupRules(group)) {
                if (rule.matchesEntity(entity)) {
                    Utils.setGlowing(entity, true, color);
                    return;
                }
            }
        }
    }

    public static boolean addRule(String name, String type, RenderColor color) {
        String key = generateRuleKey(name, type);
        boolean isNew = rules.put(key, new GlowMobRule(name, type, color)) == null;

        for (Entity entity : Utils.getEntities()) {
            applyHighlight(entity);
        }

        return isNew;
    }

    public static boolean removeRule(String name, String type) {
        String key = generateRuleKey(name, type);
        GlowMobRule removed = rules.remove(key);
        if (removed == null) return false;

        for (Entity entity : Utils.getEntities()) {
            if (removed.matches(entity)) {
                Utils.setGlowing(entity, false, RenderColor.white);
            }
        }

        return true;
    }

    public static void clearRules() {
        for (Entity entity : Utils.getEntities()) {
            if (!(entity instanceof LivingEntity)) continue;

            for (GlowMobRule rule : rules.values()) {
                if (rule.matches(entity)) {
                    Utils.setGlowing(entity, false, RenderColor.white);
                    break;
                }
            }
        }
        rules.clear();
    }

    public static Collection<GlowMobRule> getRules() {
        return List.copyOf(rules.values());
    }

    public static boolean addGroup(String group, RenderColor color) {
        ensureGroupsLoaded();

        String normalized = normalizeGroupName(group);
        if (normalized == null || !groups.containsKey(normalized)) {
            return false;
        }

        activeGroups.put(normalized, color);

        for (Entity entity : Utils.getEntities()) {
            applyHighlight(entity);
        }

        return true;
    }

    public static boolean removeGroup(String group) {
        String normalized = normalizeGroupName(group);
        if (normalized == null) return false;

        RenderColor removed = activeGroups.remove(normalized);
        if (removed == null) return false;

        for (Entity entity : Utils.getEntities()) {
            if (!(entity instanceof LivingEntity)) continue;

            for (GlowMobRule rule : getGroupRules(normalized)) {
                if (rule.matchesEntity(entity)) {
                    Utils.setGlowing(entity, false, RenderColor.white);
                    break;
                }
            }
        }

        return true;
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

    private static String generateRuleKey(String name, String type) {
        String n = normalizeRuleName(name);
        String t = (type == null || type.isEmpty()) ? "ANY" : type.toLowerCase();
        return (n == null ? "ANY" : n) + ":" + t;
    }

    private static String normalizeRuleName(String name) {
        if (name == null) return null;

        String n = name.trim().toLowerCase();
        if (n.isEmpty()) return null;

        if (n.startsWith("#")) {
            return "#" + normalizeGroupName(n);
        }

        return n.toLowerCase();
    }

    private static String normalizeGroupName(String group) {
        if (group == null) return null;

        String g = group.trim();
        if (g.startsWith("#")) {
            g = g.substring(1);
        }

        g = g.trim().toLowerCase();
        return g.isEmpty() ? null : g;
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

    public static class GlowMobRule {
        public String name;
        public String type;
        public RenderColor color;

        public GlowMobRule(String name, String type, RenderColor color) {
            this.name = normalizeRuleName(name);

            if (type != null && !type.isEmpty()) {
                this.type = Utils.stripPrefix(type, "minecraft:").toLowerCase();
            } else {
                this.type = null;
            }

            this.color = color;
        }

        public boolean matches(Entity entity) {
            if (this.name != null && this.name.startsWith("#")) {
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

        private boolean matchesEntity(Entity entity) {
            if (this.name != null) {
                String entityName = Utils.toPlain(entity.getName());
                if (!entityName.toLowerCase().contains(this.name.toLowerCase())) {
                    return false;
                }
            }

            if (this.type != null) {
                String typeStr = entity.getType().toString();
                typeStr = Utils.stripPrefix(typeStr, "entity.minecraft.");
                typeStr = typeStr.toLowerCase();
                return typeStr.equals(this.type);
            }

            return true;
        }
    }
}