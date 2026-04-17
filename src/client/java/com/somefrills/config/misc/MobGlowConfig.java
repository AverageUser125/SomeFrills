package com.somefrills.config.misc;

import com.google.gson.annotations.Expose;
import com.somefrills.features.misc.matcher.MatchInfo;
import com.somefrills.misc.RenderColor;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;
import kotlin.jvm.Transient;
import net.minecraft.entity.LivingEntity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class MobGlowConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Highlight entities based on name, type, or both")
    @ConfigEditorBoolean
    public Property<Boolean> enabled = Property.of(false);

    @Expose
    @ConfigOption(name = "Save Rules", desc = "Save and restore glow rules between sessions")
    @ConfigEditorBoolean
    public Property<Boolean> saveRules = Property.of(true);

    @Expose
    public List<GlowMobRule> rules = new ArrayList<>();

    public static class GlowMobRule {
        @Expose
        @NonNull
        private MatchInfo info;
        @Expose
        @NonNull
        private RenderColor color;
        @Expose
        private boolean enabled;
        @Nullable
        @Transient
        private Predicate<LivingEntity> predicate;

        // Must have no-args constructor for Gson deserialization
        public GlowMobRule() {
            this.info = new MatchInfo();
            this.color = RenderColor.white;
            this.enabled = false;
            this.predicate = null;
        }

        public GlowMobRule(GlowMobRule other) {
            this.info = new MatchInfo(other.info);
            this.color = new RenderColor(other.color.r, other.color.g, other.color.b, other.color.a);
            this.enabled = other.enabled;
            this.predicate = other.predicate; // predicate can be shared since it's derived from info
        }

        public GlowMobRule(MatchInfo info, RenderColor color) {
            this(info, color, true);
        }

        public GlowMobRule(@NonNull MatchInfo info, @NonNull RenderColor color, boolean enabled) {
            this.info = info;
            this.color = color;
            this.enabled = enabled;
            this.predicate = null;
        }

        public RenderColor color() {
            return color;
        }

        public MatchInfo info() {
            return info;
        }

        public void recompilePredicate() {
            this.predicate = null;
        }

        public boolean enabled() {
            return enabled;
        }

        public void toggle() {
            this.enabled = !this.enabled;
        }

        public @NonNull Predicate<LivingEntity> predicate() {
            if(this.predicate == null) {
                this.predicate = info.compile();
            }
            return predicate;
        }

        public boolean matches(LivingEntity entity) {
            return predicate().test(entity);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null) return false;
            if(!(o instanceof GlowMobRule that)) return false;

            if (enabled != that.enabled) return false;
            if (!Objects.equals(info, that.info)) return false;
            return Objects.equals(color, that.color);
        }

        @Override
        public int hashCode() {
            int result = info.hashCode();
            result = 31 * result + color.hashCode();
            result = 31 * result + (enabled ? 1 : 0);
            return result;
        }

        public void set(GlowMobRule other) {
            if (other == null) return;
            this.info = new MatchInfo(other.info);
            this.color = new RenderColor(other.color.r, other.color.g, other.color.b, other.color.a);
            this.enabled = other.enabled;
            this.predicate = other.predicate;
        }
    }
}