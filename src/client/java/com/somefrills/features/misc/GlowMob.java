package com.somefrills.features.misc;

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
import net.minecraft.entity.player.PlayerEntity;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Entity highlighting feature that allows users to highlight entities based on name, type, or both.
 * Supports commands like /entityhighlight, /entityhighlight list, /entityhighlight clear
 */
public class GlowMob extends Feature {
    private static final ConcurrentHashMap<String, GlowMobRule> rules = new ConcurrentHashMap<>();

    public GlowMob() {
        super(FrillsConfig.instance.misc.glowMob.enabled);
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onEntityUpdate(EntityUpdatedEvent event) {
        if (!isActive()) return;

        Entity entity = event.entity;
        if (!(entity instanceof LivingEntity)) return;

        if (entity instanceof PlayerEntity) return;

        applyHighlight(entity);
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onServerJoin(ServerJoinEvent event) {
        for (Entity entity: Utils.getEntities()) {
            applyHighlight(entity);
        }
    }

    private static void applyHighlight(Entity entity) {
        for (GlowMobRule rule : rules.values()) {
            if (!rule.matches(entity)) {
                continue;
            }
            Utils.setGlowing(entity, true, rule.color);
            return;
        }
        Utils.setGlowing(entity, false, RenderColor.white);
    }

    /**
     * Add or update an entity highlight rule
     */
    public static boolean addRule(String name, String type, RenderColor color) {
        String ruleKey = generateRuleKey(name, type);
        boolean isNew = rules.put(ruleKey, new GlowMobRule(name, type, color)) == null;
        updateAllEntities();
        return isNew;
    }

    /**
     * Remove an entity highlight rule
     */
    public static boolean removeRule(String name, String type) {
        String ruleKey = generateRuleKey(name, type);
        boolean removed = rules.remove(ruleKey) != null;
        if (removed) {
            updateAllEntities();
        }
        return removed;
    }

    /**
     * Clear all rules
     */
    public static void clearRules() {
        rules.clear();
        updateAllEntities();
    }

    /**
     * Update all existing entities with current rules
     */
    private static void updateAllEntities() {
        for (Entity entity : Utils.getEntities()) {
            if (!(entity instanceof LivingEntity) || entity instanceof PlayerEntity) {
                continue;
            }
            applyHighlight(entity);
        }
    }

    /**
     * Get all rules
     */
    public static Collection<GlowMobRule> getRules() {
        return List.copyOf(rules.values());
    }

    /**
     * Generate a unique key for a rule based on name and type
     */
    private static String generateRuleKey(String name, String type) {
        String n = (name == null || name.isEmpty()) ? "ANY" : name.toLowerCase();
        String t = (type == null || type.isEmpty()) ? "ANY" : type.toLowerCase();
        return n + ":" + t;
    }

    /**
     * Data class to store an entity highlighting rule
     */
    public static class GlowMobRule {
        public String name;
        public String type;
        public RenderColor color;

        public GlowMobRule(String name, String type, RenderColor color) {
            this.name = (name == null || name.isEmpty()) ? null : name;
            // Normalize type: strip minecraft: prefix if present and store lowercase
            if (type != null && !type.isEmpty()) {
                this.type = Utils.stripPrefix(type, "minecraft:").toLowerCase();
            } else {
                this.type = null;
            }
            this.color = color;
        }

        public boolean matches(Entity entity) {
            // Check name match
            if (this.name != null && !this.name.isEmpty()) {
                String entityName = Utils.toPlain(entity.getName());
                if (!entityName.toLowerCase().contains(this.name.toLowerCase())) {
                    return false;
                }
            }

            // Check type match (exact match if rule has a type)
            if (this.type != null && !this.type.isEmpty()) {
                String entityTypeStr = entity.getType().toString();
                entityTypeStr = Utils.stripPrefix(entityTypeStr, "entity.minecraft.");
                entityTypeStr = entityTypeStr.toLowerCase();
                if (!entityTypeStr.equals(this.type)) {
                    return false;
                }
            }

            return true;
        }
    }
}

