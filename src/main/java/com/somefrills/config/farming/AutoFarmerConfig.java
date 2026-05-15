package com.somefrills.config.farming;

import com.google.gson.annotations.Expose;
import com.somefrills.features.farming.autofarmer.CropType;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;
import org.lwjgl.glfw.GLFW;

public class AutoFarmerConfig {
    @Expose
    @ConfigOption(name = "Auto farmer", desc = "")
    @ConfigEditorBoolean
    public Property<Boolean> enabled = Property.of(false);

    @Expose
    @ConfigOption(name = "Toggle keybind", desc = "Keybind to enable/disable farming (default: space)")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_SPACE)
    public Property<Integer> keybind = Property.of(GLFW.GLFW_KEY_SPACE);

    @Expose
    @ConfigOption(name = "State change keybind", desc = "Keybind to cycle to next movement state (default: G)")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_G)
    public Property<Integer> stateChangeKeybind = Property.of(GLFW.GLFW_KEY_G);

    @Expose
    @ConfigOption(name = "Crop Type", desc = "Type of crop to farm")
    @ConfigEditorDropdown
    public Property<CropType> cropType = Property.of(CropType.MELON);

    public CropType getCropType() {
        return cropType.get();
    }
}