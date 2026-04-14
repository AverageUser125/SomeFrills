package com.somefrills.config.misc;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

import java.util.ArrayList;
import java.util.List;

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
    public List<RuleData> rules = new ArrayList<>();

    public static class RuleData {
        @Expose
        public String id;
        @Expose
        public String matcherExpression;
        @Expose
        public int colorHex;

        public RuleData(String id, String matcherExpression, int colorHex) {
            this.id = id;
            this.matcherExpression = matcherExpression;
            this.colorHex = colorHex;
        }
    }
}