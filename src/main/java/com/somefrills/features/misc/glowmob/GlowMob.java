package com.somefrills.features.misc.glowmob;

import com.somefrills.config.FrillsConfig;
import com.somefrills.config.misc.MobGlowConfig.GlowMobRule;
import com.somefrills.events.GameStopEvent;
import com.somefrills.events.TickEventPost;
import com.somefrills.features.core.Feature;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GlowMob extends Feature {

    private final List<GlowMobRule> rules;
    private List<LivingEntity> entityList = List.of();

    public GlowMob() {
        super(FrillsConfig.instance.misc.glowMob.enabled);
        rules = FrillsConfig.instance.misc.glowMob.rules;
    }

    /* ---------------- ENTITY CACHE ---------------- */

    private List<LivingEntity> getEntities() {
        if (entityList == null) updateEntities();
        return entityList;
    }

    private void updateEntities() {
        entityList = Utils.getEntities().stream()
                .filter(Utils::isMob)
                .map(LivingEntity.class::cast)
                .toList();
    }

    /* ---------------- CORE MATCHING ---------------- */

    private RenderColor findGlowMatch(LivingEntity living) {
        for (GlowMobRule rule : rules) {
            if (rule == null || !rule.enabled()) continue;
            if (rule.matches(living)) return rule.color();
        }
        return null;
    }

    private void applyHighlight(LivingEntity living) {
        RenderColor color = findGlowMatch(living);
        if (color != null) {
            Utils.setGlowing(living, true, color);
        }
    }

    private void forEachMatching(GlowMobRule rule, Consumer<LivingEntity> action) {
        if (rule == null) return;

        for (LivingEntity living : getEntities()) {
            if (rule.matches(living)) {
                action.accept(living);
            }
        }
    }

    private void clearGlow(LivingEntity living) {
        Utils.setGlowing(living, false, RenderColor.white);
    }

    private void applyGlow(LivingEntity living, GlowMobRule rule) {
        Utils.setGlowing(living, true, rule.color());
    }

    /* ---------------- LIFECYCLE ---------------- */

    @Override
    protected void onDeactivate() {
        updateEntities();
        for (LivingEntity living : getEntities()) {
            for (GlowMobRule rule : rules) {
                if (rule.matches(living)) {
                    clearGlow(living);
                    break;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onWorldTick(TickEventPost event) {
        updateEntities();
        getEntities().forEach(this::applyHighlight);
    }

    @EventHandler
    private void onGameClose(GameStopEvent event) {
        if (FrillsConfig.instance.misc.glowMob.saveRules) return;
        clearRules();
    }

    /* ---------------- RULE MANAGEMENT ---------------- */

    public int addRule(GlowMobRule rule) {
        if (!isActive() || rule == null) return -1;

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

    public boolean removeRule(int id) {
        if (!isActive()) return false;

        GlowMobRule removed = rules.remove(id - 1);
        if (removed == null) return false;

        updateEntities();
        forEachMatching(removed, this::clearGlow);

        return true;
    }

    public void replaceRule(GlowMobRule original, GlowMobRule replacement) {
        if (!isActive()) return;

        int idx = rules.indexOf(original);
        if (idx == -1) {
            Utils.info("Original rule not found, cannot replace");
            return;
        }

        rules.set(idx, replacement);
        updateEntities();

        for (LivingEntity living : getEntities()) {
            boolean oldMatch = original.matches(living);
            boolean newMatch = replacement.matches(living);

            if (oldMatch && !newMatch) {
                clearGlow(living);
            } else if (!oldMatch && newMatch) {
                applyGlow(living, replacement);
            }
        }
    }

    public void clearRules() {
        if (!isActive()) return;

        updateEntities();

        for (GlowMobRule rule : rules) {
            forEachMatching(rule, this::clearGlow);
        }

        rules.clear();
    }

    public void toggleRule(int parsedId) {
        if (!isActive()) return;

        GlowMobRule rule = rules.get(parsedId - 1);
        if (rule == null) return;

        updateEntities();

        if (rule.enabled()) {
            forEachMatching(rule, this::clearGlow);
        } else {
            forEachMatching(rule, living -> applyGlow(living, rule));
        }

        rule.toggle();
    }

    /* ---------------- QUERY API ---------------- */

    public List<GlowMobRule> getRules() {
        return rules;
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
            if (!rule.enabled()) continue;

            List<LivingEntity> matchedEntities = getEntities().stream()
                    .filter(rule::matches)
                    .toList();

            result.add(new MatchedEntityEntry(rule, matchedEntities));
        }

        return result;
    }
}