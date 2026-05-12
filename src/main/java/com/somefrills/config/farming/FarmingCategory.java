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
    @ConfigOption(name = "Auto farmer", desc = "Keybind to farm")
    @Accordion
    public AutoFarmerConfig autoFarmer = new AutoFarmerConfig();
}
