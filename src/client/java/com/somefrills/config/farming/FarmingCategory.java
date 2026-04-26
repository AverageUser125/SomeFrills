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
    @ConfigOption(name = "Space farmer", desc = "Farm with space bar while holding shift ")
    @ConfigEditorBoolean
    public Property<Boolean> spaceFarmerEnabled = Property.of(false);

    @Expose
    @Accordion
    @ConfigOption(name = "Auto Farm", desc = "Automatically farm wheat")
    public AutoFarmConfig autoFarm = new AutoFarmConfig();

    public static class AutoFarmConfig {
        @Expose
        @ConfigOption(name = "Enabled", desc = "Automatically farm wheat")
        @ConfigEditorBoolean
        public Property<Boolean> enabled = Property.of(false);

        @Expose
        @ConfigOption(name = "Toggle Key", desc = "Key to toggle AutoFarm on/off")
        @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_GRAVE_ACCENT)
        public int toggleKey = GLFW.GLFW_KEY_GRAVE_ACCENT;
    }
}
