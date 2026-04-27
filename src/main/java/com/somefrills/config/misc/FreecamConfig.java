package com.somefrills.config.misc;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;
import org.lwjgl.glfw.GLFW;

public class FreecamConfig {
    @Expose
    @ConfigOption(
            name = "Enabled",
            desc = "Enable freecam mode to move your camera independently of your player"
    )
    @ConfigEditorBoolean
    public Property<Boolean> enabled = Property.of(false);

    @Expose
    @ConfigOption(
            name = "Keybind",
            desc = "Keybind to toggle freecam mode"
    )
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
    public Property<Integer> keybind = Property.of(GLFW.GLFW_KEY_UNKNOWN);

    @Expose
    @ConfigOption(
            name = "Speed",
            desc = "Movement speed in freecam"
    )
    @ConfigEditorSlider(minValue = 0.0f, maxValue = 10.0f, minStep = 0.1F)
    public double speed = 1.0;

    @Expose
    @ConfigOption(
            name = "Scroll Sensitivity",
            desc = "Adjust speed using mouse wheel (0 disables)"
    )
    @ConfigEditorSlider(minValue = 0.0f, maxValue = 2.0f, minStep = 0.1F)
    public double speedScrollSensitivity = 0.1;

    @Expose
    @ConfigOption(
            name = "Reload Chunks",
            desc = "Prevents rendering artifacts in freecam"
    )
    @ConfigEditorBoolean
    public boolean reloadChunks = true;

    @Expose
    @ConfigOption(
            name = "Render Hands",
            desc = "Render player hands in freecam view"
    )
    @ConfigEditorBoolean
    public boolean renderHands = true;

    @Expose
    @ConfigOption(
            name = "Static View",
            desc = "Prevents external forces from moving camera"
    )
    @ConfigEditorBoolean
    public boolean staticView = true;

    @Expose
    @ConfigOption(
            name = "Toggle on Death",
            desc = "Automatically toggle freecam on when you die"
    )
    @ConfigEditorBoolean
    public boolean toggleOnDeath = true;

    @Expose
    @ConfigOption(
            name = "Toggle on Damage",
            desc = "Automatically toggle freecam on when you take damage"
    )
    @ConfigEditorBoolean
    public boolean toggleOnDamage = true;
}