package com.somefrills.config.misc

import com.google.gson.annotations.Expose
import com.somefrills.features.misc.glowmob.MatchInfo
import com.somefrills.misc.RenderColor
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property
import net.minecraft.world.entity.LivingEntity
import java.util.function.Predicate

class GlowMobConfig {
    @JvmField
    @Expose
    @ConfigOption(name = "Enabled", desc = "Highlight entities based on name, type, or both")
    @ConfigEditorBoolean
    var enabled: Property<Boolean> = Property.of(false)

    @JvmField
    @Expose
    @ConfigOption(name = "Save Rules", desc = "Save and restore glow rules between sessions")
    @ConfigEditorBoolean
    var saveRules: Boolean = true

    @JvmField
    @Expose
    var rules: MutableList<GlowMobRule> = ArrayList<GlowMobRule>()

    class GlowMobRule {
        @Expose
        private var info: MatchInfo

        @Expose
        private var color: RenderColor

        @Expose
        private var enabled: Boolean

        @Transient
        private var predicate: Predicate<LivingEntity>?

        // Must have no-args constructor for Gson deserialization
        constructor() {
            this.info = MatchInfo()
            this.color = RenderColor.white
            this.enabled = false
            this.predicate = null
        }

        constructor(other: GlowMobRule) {
            this.info = MatchInfo(other.info)
            this.color = RenderColor(other.color)
            this.enabled = other.enabled
            this.predicate = other.predicate // predicate can be shared since it's derived from info
        }

        @JvmOverloads
        constructor(info: MatchInfo, color: RenderColor, enabled: Boolean = false) {
            this.info = MatchInfo(info)
            this.color = RenderColor(color)
            this.enabled = enabled
            this.predicate = null
        }

        fun color(): RenderColor {
            return color
        }

        fun info(): MatchInfo {
            return info
        }

        fun recompilePredicate() {
            this.predicate = null
        }

        fun enabled(): Boolean {
            return enabled
        }

        fun toggle() {
            this.enabled = !this.enabled
        }

        fun matches(entity: LivingEntity): Boolean {
            if (this.predicate == null) {
                this.predicate = info.compile()
            }
            return predicate!!.test(entity)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null) return false
            if (other !is GlowMobRule) return false

            if (enabled != other.enabled) return false
            if (info != other.info) return false
            return color == other.color
        }

        override fun hashCode(): Int {
            var result = info.hashCode()
            result = 31 * result + color.hashCode()
            result = 31 * result + (if (enabled) 1 else 0)
            return result
        }

        fun set(other: GlowMobRule?) {
            if (other == null) return
            this.info = MatchInfo(other.info)
            this.color = RenderColor(other.color.r, other.color.g, other.color.b, other.color.a)
            this.enabled = other.enabled
            this.predicate = other.predicate
        }
    }
}