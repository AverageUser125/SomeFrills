package com.somefrills.config.farming

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property

class FarmingCategory {
    @JvmField
    @Expose
    @ConfigOption(name = "Auto warp home", desc = "Automatically warp home when taking fatal damage")
    @ConfigEditorBoolean
    var autoWarpHomeEnabled: Property<Boolean> = Property.of(false)

    @JvmField
    @Expose
    @ConfigOption(name = "Auto pest set home", desc = "Automatically set home when pests spawn")
    @ConfigEditorBoolean
    var autoPestSetHomeEnabled: Property<Boolean> = Property.of(false)

    @JvmField
    @Expose
    @ConfigOption(name = "Auto farmer", desc = "Keybind to farm")
    @Accordion
    var autoFarmer: AutoFarmerConfig = AutoFarmerConfig()
}
