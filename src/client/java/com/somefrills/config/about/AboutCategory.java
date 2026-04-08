package com.somefrills.config.about;

import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;
import kotlin.Unit;
import kotlin.jvm.Transient;
import net.minecraft.util.Util;

public class AboutCategory {

    @Transient
    @ConfigOption(name = "Current Version", desc = "The SomeFrills version you are currently running")
    @ConfigVersionDisplay
    public Unit currentVersion = null;

    @ConfigOption(name = "Check for Updates", desc = "Automatically check for updates on startup")
    @ConfigEditorBoolean
    public Property<Boolean> checkForUpdates = Property.of(true);

    @ConfigOption(name = "Auto Updates", desc = "Automatically download new versions on startup")
    @ConfigEditorBoolean
    public boolean fullAutoUpdates = false;

    @ConfigOption(name = "Used Software", desc = "Information about used software and licenses")
    @Accordion
    public Licenses licenses = new Licenses();

    public static class Licenses {
        @ConfigOption(name = "MoulConfig", desc = "MoulConfig is available under the LGPL 3.0 License or later version")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable moulConfig = () -> openBrowser("https://github.com/NotEnoughUpdates/MoulConfig");

        @ConfigOption(name = "LibAutoUpdate", desc = "LibAutoUpdate is available under the BSD 2 Clause License")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable libAutoUpdate = () -> openBrowser("https://github.com/nea89o/libautoupdate");

        @ConfigOption(name = "Mixin", desc = "Mixin is available under the MIT License")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable mixin = () -> openBrowser("https://github.com/SpongePowered/Mixin/");

        @ConfigOption(name = "Guava", desc = "Guava is available under the Apache License 2.0")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable guava = () -> openBrowser("https://github.com/google/guava");

        @ConfigOption(name = "Orbit", desc = "Orbit is available under the MIT License")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable orbit = () -> openBrowser("https://github.com/MeteorDevelopment/orbit");

        @ConfigOption(name = "SkyHanni", desc = "SkyHanni is available under the LGPL 2.1 License")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable skyHanni = () -> openBrowser("https://github.com/hannibal002/SkyHanni");

        private static void openBrowser(String url) {
            Util.getOperatingSystem().open(url);
        }
    }
}




