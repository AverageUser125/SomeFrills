package com.somefrills.config.farming;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;
import org.lwjgl.glfw.GLFW;

public class FarmingCategory {
    @Expose
    @ConfigOption(name = "Auto warp home", desc = "Automatically warp home when taking fatal damage")
    @ConfigEditorBoolean
    public Property<Boolean> autoWarpHomeEnabled = Property.of(false);

    @Expose
    @ConfigOption(name = "Auto pest set home", desc = "Automatically set home when pests spawn")
    @ConfigEditorBoolean
    public Property<Boolean> autoPestSetHomeEnabled = Property.of(false);


    @Expose
    @ConfigOption(name = "Space farmer", desc = "Keybind to farm")
    @Accordion
    public SpaceFarmerConfig spaceFarmer = new SpaceFarmerConfig();

    public static class SpaceFarmerConfig {
        @Expose
        @ConfigOption(name = "Space farmer", desc = "Farm with space bar while holding shift ")
        @ConfigEditorBoolean
        public Property<Boolean> enabled = Property.of(false);

        @Expose
        @ConfigOption(name = "Keybind", desc = "Keybind to farm (default: space)")
        @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_SPACE)
        public Property<Integer> keybind = Property.of(GLFW.GLFW_KEY_SPACE);

        @Expose
        @ConfigOption(name = "Hold forward key", desc = "Hold forward key while farming")
        @ConfigEditorBoolean
        public boolean forwardKey;
    }
}
