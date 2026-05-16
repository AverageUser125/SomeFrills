package com.somefrills.config.misc

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property
import org.lwjgl.glfw.GLFW

class FreecamConfig {
    @JvmField
    @Expose
    @ConfigOption(name = "Enabled", desc = "Enable freecam mode to move your camera independently of your player")
    @ConfigEditorBoolean
    var enabled: Property<Boolean> = Property.of(false)

    @JvmField
    @Expose
    @ConfigOption(name = "Keybind", desc = "Keybind to toggle freecam mode")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
    var keybind: Property<Int?> = Property.of<Int?>(GLFW.GLFW_KEY_UNKNOWN)

    @JvmField
    @Expose
    @ConfigOption(name = "Speed", desc = "Movement speed in freecam")
    @ConfigEditorSlider(minValue = 0.0f, maxValue = 10.0f, minStep = 0.1f)
    var speed: Double = 1.0

    @JvmField
    @Expose
    @ConfigOption(name = "Scroll Sensitivity", desc = "Adjust speed using mouse wheel (0 disables)")
    @ConfigEditorSlider(minValue = 0.0f, maxValue = 2.0f, minStep = 0.1f)
    var speedScrollSensitivity: Double = 0.1

    @JvmField
    @Expose
    @ConfigOption(name = "Reload Chunks", desc = "Prevents rendering artifacts in freecam")
    @ConfigEditorBoolean
    var reloadChunks: Boolean = true

    @JvmField
    @Expose
    @ConfigOption(name = "Render Hands", desc = "Render player hands in freecam view")
    @ConfigEditorBoolean
    var renderHands: Boolean = true

    @JvmField
    @Expose
    @ConfigOption(name = "Static View", desc = "Prevents external forces from moving camera")
    @ConfigEditorBoolean
    var staticView: Boolean = true

    @JvmField
    @Expose
    @ConfigOption(name = "Toggle on Death", desc = "Automatically toggle freecam on when you die")
    @ConfigEditorBoolean
    var toggleOnDeath: Boolean = true

    @JvmField
    @Expose
    @ConfigOption(name = "Toggle on Damage", desc = "Automatically toggle freecam on when you take damage")
    @ConfigEditorBoolean
    var toggleOnDamage: Boolean = true
}