package com.somefrills.features.misc.glowmob;

import com.somefrills.config.FrillsConfig;
import com.somefrills.config.misc.MobGlowConfig.GlowMobRule;
import com.somefrills.events.TickEventPost;
import com.somefrills.features.core.Feature;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity highlighting feature that allows users to highlight entities based on custom matcher expressions.
 */
public class GlowMob extends Feature {

    private final List<GlowMobRule> rules;
    private List<Entity> entityList = List.of();

    public GlowMob() {
        super(FrillsConfig.instance.misc.glowMob.enabled);
        rules = FrillsConfig.instance.misc.glowMob.rules;
    }

    private static List<Entity> getUpdatedEntities() {
        return Utils.getEntities().stream()
                .filter(Utils::isMob)
                .toList();
    }

    /**
     * Determines if entity should glow and what color. Single pass through all rules.
     * Returns null if no match found.
     */
    private RenderColor findGlowMatch(Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity)) return null;

        for (GlowMobRule rule : rules) {
            if (rule == null) continue;
            // Skip disabled rules
            if (!rule.enabled()) continue;
            if (rule.matches(livingEntity)) {
                return rule.color();
            }
        }
        return null;
    }

    private void applyHighlight(Entity entity) {
        if (!(entity instanceof LivingEntity)) return;

        RenderColor color = findGlowMatch(entity);
        if (color != null) {
            Utils.setGlowing(entity, true, color);
        }
    }

    public int addRule(GlowMobRule rule) {
        if (rule == null) {
            return -1;
        }
        try {
            rules.add(rule);
            return rules.size();
        } catch (Exception e) {
            Utils.infoFormat("Failed to add glow rule: {}", e.getMessage());
            return -1;
        }
    }

    public int addRule(MatchInfo matcher, RenderColor color) {
        return addRule(new GlowMobRule(matcher, color));
    }

    public boolean removeRule(GlowMobRule rule) {
        return removeRule(rules.indexOf(rule) + 1);
    }

    public void replaceRule(GlowMobRule original, GlowMobRule newVersion) {
        int idx = rules.indexOf(original);
        if (idx == -1) {
            Utils.info("Original rule not found, cannot replace");
            return;
        }
        rules.set(idx, newVersion);
        // Refresh entity list and update glowing on entities that matched the old or new version of this rule
        updateEntities();
        for (Entity entity : getEntities()) {
            boolean matchesOld = original.matches((LivingEntity) entity);
            boolean matchesNew = newVersion.matches((LivingEntity) entity);
            if (matchesOld && !matchesNew) {
                Utils.setGlowing(entity, false, RenderColor.white);
            } else if (!matchesOld && matchesNew) {
                Utils.setGlowing(entity, true, newVersion.color());
            }
        }
    }

    public boolean removeRule(int id) {
        GlowMobRule removed = rules.remove(id - 1);
        if (removed != null) {
            // Refresh entity list and disable glowing on entities that matched this rule
            updateEntities();
            for (Entity entity : getEntities()) {
                if (entity instanceof LivingEntity livingEntity && removed.matches(livingEntity)) {
                    Utils.setGlowing(entity, false, RenderColor.white);
                }
            }
        }
        return removed != null;
    }

    public void clearRules() {
        updateEntities();
        for (Entity entity : getEntities()) {
            RenderColor result = findGlowMatch(entity);
            if (result != null) {
                Utils.setGlowing(entity, false, RenderColor.white);
            }
        }
        rules.clear();
    }

    public List<GlowMobRule> getRules() {
        return rules;
    }

    private List<Entity> getEntities() {
        if (entityList == null) {
            updateEntities();
        }
        return entityList;
    }

    private void updateEntities() {
        entityList = getUpdatedEntities();
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onWorldTick(TickEventPost event) {
        updateEntities();
        getEntities().forEach(this::applyHighlight);
    }

    public void toggleRule(int parsedId) {
        var rule = rules.get(parsedId - 1);
        if (rule == null) return;

        updateEntities();
        if (rule.enabled()) {
            for (Entity entity : getEntities()) {
                if (entity instanceof LivingEntity livingEntity && rule.matches(livingEntity)) {
                    Utils.setGlowing(entity, false, RenderColor.white);
                }
            }
        } else {
            for (Entity entity : getEntities()) {
                if (entity instanceof LivingEntity livingEntity && rule.matches(livingEntity)) {
                    Utils.setGlowing(entity, true, rule.color());
                }
            }
        }
        rule.toggle();
    }


    public static class MatchedEntityEntry {
        public GlowMobRule rule;
        public List<LivingEntity> entities;

        public MatchedEntityEntry(GlowMobRule rule, List<LivingEntity> entities) {
            this.rule = rule;
            this.entities = entities;
        }
    }

    public List<MatchedEntityEntry> getGlowingMobs() {
        return getGlowingMobs(rules);
    }

    public List<MatchedEntityEntry> getGlowingMobs(List<GlowMobRule> rules) {
        updateEntities();
        ArrayList<MatchedEntityEntry> result = new ArrayList<>();
        for (GlowMobRule rule : rules) {
            // TODO: put this check inside the rule.matches method
            if (!rule.enabled()) continue;

            List<LivingEntity> matchedEntities = getEntities().stream()
                    .filter(entity -> entity instanceof LivingEntity)
                    .map(entity -> (LivingEntity) entity)
                    .filter(rule::matches)
                    .toList();
            result.add(new MatchedEntityEntry(rule, matchedEntities));
        }
        return result;
    }

}
