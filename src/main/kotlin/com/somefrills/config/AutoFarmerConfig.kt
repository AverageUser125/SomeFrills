package com.somefrills.config.farming

import com.google.gson.annotations.Expose
import com.somefrills.features.farming.autofarmer.CropType
import io.github.notenoughupdates.moulconfig.annotations.*
import io.github.notenoughupdates.moulconfig.observer.Property
import org.lwjgl.glfw.GLFW

class AutoFarmerConfig {
    @JvmField
    @Expose
    @ConfigOption(name = "Auto farmer", desc = "")
    @ConfigEditorBoolean
    var enabled: Property<Boolean> = Property.of(false)

    @JvmField
    @Expose
    @ConfigOption(name = "Toggle keybind", desc = "Keybind to enable/disable farming (default: space)")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_SPACE)
    var keybind: Property<Int?> = Property.of<Int?>(GLFW.GLFW_KEY_SPACE)

    @JvmField
    @Expose
    @ConfigOption(name = "State change keybind", desc = "Keybind to cycle to next movement state (default: G)")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_G)
    var stateChangeKeybind: Property<Int?> = Property.of<Int?>(GLFW.GLFW_KEY_G)

    @JvmField
    @Expose
    @ConfigOption(name = "Crop Type", desc = "Type of crop to farm")
    @ConfigEditorDropdown
    var cropType: Property<CropType?> = Property.of<CropType?>(CropType.MELON)

    @JvmField
    @Expose
    @ConfigOption(
        name = "Restore Farming State",
        desc = "Automatically restore farming state when reactivating (after pressing toggle keybind or closing screens)"
    )
    @ConfigEditorBoolean
    var restoreState: Boolean = true

    @JvmField
    @Expose
    @ConfigOption(name = "Enable Distance Check", desc = "Only restore state if within maximum distance")
    @ConfigEditorBoolean
    var enableDistanceCheck: Boolean = true

    @JvmField
    @Expose
    @ConfigOption(name = "Max Restore Distance", desc = "Maximum distance in blocks to restore farming state (0-10)")
    @ConfigEditorSlider(minValue = 0f, maxValue = 10f, minStep = 1f)
    var maxRestoreDistance: Int = 3
}