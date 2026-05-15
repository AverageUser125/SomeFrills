package com.somefrills.config.fishing;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class FishingCategory {
    @Expose
    @ConfigOption(name = "Rare Sea Creatures Alert", desc = "Alert you when you catch a rare sea creature")
    @ConfigEditorBoolean
    public Property<Boolean> rareSeaCreatureAlert = Property.of(true);

    // auto fish by mixin into skyhanni "real now", add random delay too
    @Expose
    @Accordion
    @ConfigOption(name = "Auto Fish", desc = "Automatically reel in and cast out when fishing")
    public AutoFishConfig autoFish = new AutoFishConfig();

    public static class AutoFishConfig {
        @Expose
        @ConfigOption(name = "Enabled", desc = "Automatically reel in and cast out when fishing")
        @ConfigEditorBoolean
        public boolean enabled = false;

        @Expose
        @ConfigOption(name = "Min Delay", desc = "Minimum delay (ms) between catching a fish and recasting")
        @ConfigEditorSlider(minValue = 0, maxValue = 100, minStep = 10)
        public int minDelay = 100;

        @Expose
        @ConfigOption(name = "Max Delay", desc = "Maximum delay (ms) between catching a fish and recasting")
        @ConfigEditorSlider(minValue = 0, maxValue = 100, minStep = 10)
        public int maxDelay = 300;
    }
}
