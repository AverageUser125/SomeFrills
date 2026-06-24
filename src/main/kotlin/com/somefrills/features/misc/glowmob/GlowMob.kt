package com.somefrills.features.misc.glowmob

import com.somefrills.config.FrillsMod

import com.somefrills.config.misc.GlowMobConfig.GlowMobRule
import com.somefrills.events.GameStopEvent
import com.somefrills.events.TickEventPost
import com.somefrills.features.core.Feature
import com.somefrills.modules.FrillsFeature
import com.somefrills.misc.RenderColor
import com.somefrills.utils.ChatUtils
import com.somefrills.utils.EntityUtils
import com.somefrills.utils.isMob
import com.somefrills.utils.setGlowing
import com.somefrills.events.core.EventHandle
import com.somefrills.events.core.EventPriority
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import java.util.function.Consumer

@FrillsFeature
object GlowMob : Feature(FrillsMod.config.misc.glowMob.enabled) {
    private val config get() = FrillsMod.config.misc.glowMob

    /* ---------------- QUERY API ---------------- */
    @JvmField
    val rules: MutableList<GlowMobRule> = config.rules
    private var entityList: MutableList<LivingEntity>? = null
    private val entities: MutableList<LivingEntity>
        get() {
            if (entityList == null) updateEntities()
            return entityList!!
        }

    private fun updateEntities() {
        entityList = EntityUtils.getStreamEntities(LivingEntity::class.java)
            .filter { entity: Entity -> entity.isMob() }
            .toMutableList()
    }

    /* ---------------- CORE MATCHING ---------------- */
    private fun findGlowMatch(living: LivingEntity): RenderColor? {
        for (rule in rules) {
            if (!rule.enabled()) continue
            if (rule.matches(living)) return rule.color()
        }
        return null
    }

    private fun applyHighlight(living: LivingEntity) {
        val color = findGlowMatch(living)
        if (color != null) {
            living.setGlowing(true, color)
        }
    }

    private fun forEachMatching(rule: GlowMobRule, action: Consumer<LivingEntity>) {
        for (living in this.entities) {
            if (rule.matches(living)) {
                action.accept(living)
            }
        }
    }

    private fun clearGlow(living: LivingEntity) {
        living.setGlowing(false, RenderColor.white)
    }

    private fun applyGlow(living: LivingEntity, rule: GlowMobRule) {
        living.setGlowing(true, rule.color())
    }

    /* ---------------- LIFECYCLE ---------------- */
    override fun onDeactivate() {
        updateEntities()
        for (living in this.entities) {
            for (rule in rules) {
                if (rule.matches(living)) {
                    clearGlow(living)
                    break
                }
            }
        }
    }

    @EventHandle(priority = EventPriority.LOW)
    private fun onWorldTick(event: TickEventPost) {
        updateEntities()
        this.entities.forEach(Consumer { living: LivingEntity -> this.applyHighlight(living) })
    }

    @EventHandle
    private fun onGameClose(event: GameStopEvent) {
        if (config.saveRules) return
        clearRules()
    }

    /* ---------------- RULE MANAGEMENT ---------------- */
    fun addRule(rule: GlowMobRule): Int {
        if (!isActive()) return -1

        try {
            rules.add(rule)
            return rules.size
        } catch (e: Exception) {
            ChatUtils.infoFormat("Failed to add glow rule: {}", e.message)
            return -1
        }
    }

    fun addRule(matcher: MatchInfo, color: RenderColor): Int {
        return addRule(GlowMobRule(matcher, color))
    }

    fun removeRule(rule: GlowMobRule): Boolean {
        return removeRule(rules.indexOf(rule) + 1)
    }

    fun removeRule(id: Int): Boolean {
        if (!isActive()) return false

        val removed = rules.removeAt(id - 1)

        updateEntities()
        forEachMatching(removed) { living: LivingEntity -> this.clearGlow(living) }

        return true
    }

    fun replaceRule(original: GlowMobRule, replacement: GlowMobRule) {
        if (!isActive()) return

        val idx = rules.indexOf(original)
        if (idx == -1) {
            ChatUtils.info("Original rule not found, cannot replace")
            return
        }

        rules[idx] = replacement
        updateEntities()

        for (living in this.entities) {
            val oldMatch = original.matches(living)
            val newMatch = replacement.matches(living)

            if (oldMatch && !newMatch) {
                clearGlow(living)
            } else if (!oldMatch && newMatch) {
                applyGlow(living, replacement)
            }
        }
    }

    fun clearRules() {
        if (!isActive()) return

        updateEntities()

        for (rule in rules) {
            forEachMatching(rule) { living: LivingEntity -> this.clearGlow(living) }
        }

        rules.clear()
    }

    fun toggleRule(parsedId: Int) {
        if (!isActive()) return

        val rule = rules[parsedId - 1]

        updateEntities()

        if (rule.enabled()) {
            forEachMatching(rule) { living: LivingEntity -> this.clearGlow(living) }
        } else {
            forEachMatching(rule) { living: LivingEntity -> applyGlow(living, rule) }
        }

        rule.toggle()
    }

    class MatchedEntityEntry(@JvmField var rule: GlowMobRule, @JvmField var entities: MutableList<LivingEntity>)


    @JvmStatic
    fun getGlowingMobs(): MutableList<MatchedEntityEntry> {
        return getGlowingMobs(rules)
    }

    @JvmStatic
    fun getGlowingMobs(rules: List<GlowMobRule>): MutableList<MatchedEntityEntry> {
        updateEntities()

        val result = ArrayList<MatchedEntityEntry>()

        for (rule in rules) {
            if (!rule.enabled()) continue

            val matchedEntities = this.entities.stream()
                .filter { entity: LivingEntity -> rule.matches(entity) }
                .toList()

            result.add(MatchedEntityEntry(rule, matchedEntities))
        }

        return result
    }
}
