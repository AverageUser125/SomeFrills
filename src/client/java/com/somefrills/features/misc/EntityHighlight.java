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
public class EntityHighlight extends Feature {
    private static final ConcurrentHashMap<String, EntityHighlightRule> rules = new ConcurrentHashMap<>();

    public EntityHighlight() {
        super(FrillsConfig.instance.misc.entityHighlight.enabled);
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onEntityUpdate(EntityUpdatedEvent event) {
        if (!isActive()) return;

        Entity entity = event.entity;
        if (!(entity instanceof LivingEntity)) return;

        // Don't highlight players
        if (entity instanceof PlayerEntity) return;

        highlightEntity(entity);
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onServerJoin(ServerJoinEvent event) {
        for (Entity entity: Utils.getEntities()) {
            highlightEntity(entity);
        }
    }

    private void highlightEntity(Entity entity) {
        for (EntityHighlightRule rule : rules.values()) {
            if (!rule.matches(entity)) {
                continue;
            }
            Utils.setGlowing(entity, true, rule.color);
            return;
        }
    }

    /**
     * Add or update an entity highlight rule
     */
    public static boolean addRule(String name, String type, RenderColor color) {
        String ruleKey = generateRuleKey(name, type);
        return rules.put(ruleKey, new EntityHighlightRule(name, type, color)) == null;
    }

    /**
     * Remove an entity highlight rule
     */
    public static boolean removeRule(String name, String type) {
        String ruleKey = generateRuleKey(name, type);
        return rules.remove(ruleKey) != null;
    }

    /**
     * Clear all rules
     */
    public static void clearRules() {
        rules.clear();
    }

    /**
     * Get all rules
     */
    public static Collection<EntityHighlightRule> getRules() {
        return List.copyOf(rules.values());
    }

    /**
     * Generate a unique key for a rule based on name and type
     */
    private static String generateRuleKey(String name, String type) {
        String n = (name == null || name.isEmpty() || name.equalsIgnoreCase("none")) ? "ANY" : name.toLowerCase();
        String t = (type == null || type.isEmpty() || type.equalsIgnoreCase("none")) ? "ANY" : type.toLowerCase();
        return n + ":" + t;
    }

    /**
     * Data class to store an entity highlighting rule
     */
    public static class EntityHighlightRule {
        public String name;
        public String type;
        public RenderColor color;

        public EntityHighlightRule(String name, String type, RenderColor color) {
            this.name = (name == null || name.isEmpty()) ? null : name;
            this.type = (type == null || type.isEmpty()) ? null : type;
            this.color = color;
        }
        
        public boolean matches(Entity entity) {
            if (this.name != null && !this.name.isEmpty() && !this.name.equalsIgnoreCase("none")) {
                String entityName = Utils.toPlain(entity.getName());
                if (!entityName.toLowerCase().contains(this.name.toLowerCase())) {
                    return false;
                }
            }

            // Check type match (exact match if rule has a type)
            if (this.type != null && !this.type.isEmpty() && !this.type.equalsIgnoreCase("none")) {
                String entityTypeStr = entity.getType().toString();
                if (!entityTypeStr.equalsIgnoreCase(this.type)) {
                    return false;
                }
            }

            return true;
        }
    }
}

