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

    @Expose
    @Accordion
    @ConfigOption(name = "Auto Fish", desc = "Automatically reel in and cast out when fishing")
    public AutoFishConfig autoFish = new AutoFishConfig();

    public static class AutoFishConfig {
        @Expose
        @ConfigOption(name = "Enabled", desc = "Automatically reel in and cast out when fishing")
        @ConfigEditorBoolean
        public boolean enabled = false;
    }
}
