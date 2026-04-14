package com.somefrills.features.misc;

import com.somefrills.config.Feature;
import com.somefrills.config.FrillsConfig;
import com.somefrills.config.misc.MiscCategory;
import com.somefrills.config.misc.MiscCategory.MobGlowConfig.RuleData;
import com.somefrills.events.GameStopEvent;
import com.somefrills.events.WorldTickEvent;
import com.somefrills.features.misc.matcher.Matcher;
import com.somefrills.features.misc.matcher.MatcherParser;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Entity highlighting feature that allows users to highlight entities based on custom matcher expressions.
 */
public class GlowMob extends Feature {

    private final ConcurrentHashMap<String, GlowMobRule> rules;
    private List<Entity> entityList;

    public GlowMob() {
        super(FrillsConfig.instance.misc.glowMob.enabled);
        rules = new ConcurrentHashMap<>();
        loadRulesFromConfig();
    }

    /**
     * Load rules from config if saving is enabled
     */
    private void loadRulesFromConfig() {
        if (!FrillsConfig.instance.misc.glowMob.saveRules.get()) {
            return;
        }
        if (rules == null) {
            return;
        }

        List<RuleData> savedRules = FrillsConfig.instance.misc.glowMob.rules;
        for (RuleData ruleData : savedRules) {
            try {
                Matcher matcher = MatcherParser.parse(ruleData.matcherExpression);
                RenderColor color = RenderColor.fromHex(ruleData.colorHex);
                addRule(ruleData.id, matcher, color);
            } catch (MatcherParser.MatcherParseException e) {
                Utils.infoFormat("Failed to load glow rule '{}': {}", ruleData.id, e.getMessage());
            }
        }
    }

    @Override
    public void onEnable() {
        loadRulesFromConfig();
    }

    @EventHandler
    public void onLeave(GameStopEvent event) {
        if (!FrillsConfig.instance.misc.glowMob.saveRules.get()) {
            return;
        }

        FrillsConfig.instance.misc.glowMob.rules.clear();
        for (GlowMobRule rule : rules.values()) {
            FrillsConfig.instance.misc.glowMob.rules.add(
                    new MiscCategory.MobGlowConfig.RuleData(rule.id, rule.matcherExpression, rule.color.hex)
            );
        }
    }

    /**
     * Determines if entity should glow and what color. Single pass through all rules.
     * Returns null if no match found.
     */
    private GlowMatchResult findGlowMatch(Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity)) return null;

        // Ignore mobs that are in the dying animation
        if (livingEntity.deathTime > 0) return null;
        // Ignore mobs that are newly spawned (armor and other visuals haven't loaded yet)
        if (entity.age <= 2) return null;

        for (GlowMobRule rule : rules.values()) {
            if (rule.matches(livingEntity)) {
                return new GlowMatchResult(rule.color, rule.id);
            }
        }
        return null;
    }

    private void applyHighlight(Entity entity) {
        if (!(entity instanceof LivingEntity)) return;

        GlowMatchResult result = findGlowMatch(entity);
        if (result != null) {
            Utils.setGlowing(entity, true, result.color);
        }
    }

    public boolean addRule(String id, Matcher matcher, RenderColor color) {
        if (id == null || id.trim().isEmpty() || matcher == null || color == null) {
            return false;
        }
        String normalizedId = id.trim();

        // Check if rule already exists - don't overwrite
        if (rules.containsKey(normalizedId)) {
            return false;
        }

        try {
            rules.put(normalizedId, new GlowMobRule(normalizedId, matcher, color));
            return true;
        } catch (Exception e) {
            Utils.infoFormat("Failed to add glow rule: {}", e.getMessage());
            return false;
        }
    }

    public boolean removeRule(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        String normalizedId = id.trim();
        GlowMobRule removed = rules.remove(normalizedId);
        if (removed == null) return false;
        // Refresh entity list and disable glowing on entities that matched this rule
        updateEntities();
        for (Entity entity : getEntities()) {
            if (entity instanceof LivingEntity livingEntity && removed.matches(livingEntity)) {
                Utils.setGlowing(entity, false, RenderColor.white);
            }
        }
        return true;
    }

    public void clearRules() {
        updateEntities();
        for (Entity entity : getEntities()) {
            GlowMatchResult result = findGlowMatch(entity);
            if (result != null) {
                Utils.setGlowing(entity, false, RenderColor.white);
            }
        }
        rules.clear();
    }

    public Collection<GlowMobRule> getRules() {
        return List.copyOf(rules.values());
    }

    private List<Entity> getEntities() {
        if (entityList == null) {
            updateEntities();
        }
        return entityList;
    }

    private static List<Entity> getUpdatedEntities() {
        return Utils.getEntities().stream()
                .filter(Utils::isMob)
                .toList();
    }

    private void updateEntities() {
        entityList = getUpdatedEntities();
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onWorldTick(WorldTickEvent event) {
        if (!isActive()) return;
        updateEntities();
        getEntities().forEach(this::applyHighlight);
    }

    /**
     * Result of glow matching: color and rule ID
     */
    private record GlowMatchResult(RenderColor color, String ruleId) {
    }

    /**
     * Rule definition: ID, matcher expression, and color
     */
    public static class GlowMobRule {
        private final String id;
        private final String matcherExpression;
        private final RenderColor color;
        private final Predicate<LivingEntity> predicate;

        public GlowMobRule(String id, Matcher matcher, RenderColor color) {
            if (id == null || id.trim().isEmpty() || matcher == null || color == null) {
                throw new IllegalArgumentException("Invalid rule parameters");
            }
            this.id = id;
            this.matcherExpression = matcher.toString();
            this.color = color;
            this.predicate = matcher.compile();
        }

        public String id() {
            return id;
        }

        public RenderColor color() {
            return color;
        }

        public boolean matches(LivingEntity entity) {
            return predicate.test(entity);
        }
    }
}
