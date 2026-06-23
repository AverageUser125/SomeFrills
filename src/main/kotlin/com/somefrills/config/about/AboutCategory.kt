package com.somefrills.config.about

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property
import net.minecraft.util.Util

class AboutCategory {
    @Transient
    @ConfigOption(name = "Current Version", desc = "The SomeFrills version you are currently running")
    @ConfigVersionDisplay
    var currentVersion: Unit = Unit

    @JvmField
    @Expose
    @ConfigOption(name = "Check for Updates", desc = "Automatically check for updates on startup")
    @ConfigEditorBoolean
    var checkForUpdates: Property<Boolean> = Property.of(true)

    @JvmField
    @Expose
    @ConfigOption(name = "Auto Updates", desc = "Automatically download new versions on startup")
    @ConfigEditorBoolean
    var fullAutoUpdates: Boolean = false

    @Transient
    @ConfigOption(name = "Used Software", desc = "Information about used software and licenses")
    @Accordion
    var licenses: Licenses = Licenses()

    class Licenses {
        @Transient
        @ConfigOption(name = "MoulConfig", desc = "MoulConfig is available under the LGPL 3.0 License or later version")
        @ConfigEditorButton(buttonText = "Source")
        var moulConfig: Runnable = Runnable { openBrowser("https://github.com/NotEnoughUpdates/MoulConfig") }

        @Transient
        @ConfigOption(name = "LibAutoUpdate", desc = "LibAutoUpdate is available under the BSD 2 Clause License")
        @ConfigEditorButton(buttonText = "Source")
        var libAutoUpdate: Runnable = Runnable { openBrowser("https://github.com/nea89o/libautoupdate") }

        @Transient
        @ConfigOption(name = "Mixin", desc = "Mixin is available under the MIT License")
        @ConfigEditorButton(buttonText = "Source")
        var mixin: Runnable = Runnable { openBrowser("https://github.com/SpongePowered/Mixin/") }

        @Transient
        @ConfigOption(name = "Guava", desc = "Guava is available under the Apache License 2.0")
        @ConfigEditorButton(buttonText = "Source")
        var guava: Runnable = Runnable { openBrowser("https://github.com/google/guava") }

        @Transient
        @ConfigOption(name = "Orbit", desc = "Orbit is available under the MIT License")
        @ConfigEditorButton(buttonText = "Source")
        var orbit: Runnable = Runnable { openBrowser("https://github.com/MeteorDevelopment/orbit") }

        @Transient
        @ConfigOption(name = "SkyHanni", desc = "SkyHanni is available under the LGPL 2.1 License")
        @ConfigEditorButton(buttonText = "Source")
        var skyHanni: Runnable = Runnable { openBrowser("https://github.com/hannibal002/SkyHanni") }

        @Transient
        @ConfigOption(name = "Meteor Client", desc = "Meteor Client is available under the GPL-3.0 License")
        @ConfigEditorButton(buttonText = "Source")
        var meteorClient: Runnable = Runnable { openBrowser("https://github.com/MeteorDevelopment/meteor-client") }

        companion object {
            private fun openBrowser(url: String) {
                Util.getPlatform().openUri(url)
            }
        }
    }
}




