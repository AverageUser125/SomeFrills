package com.somefrills.config.about;

import com.google.gson.annotations.Expose;
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
    public Unit currentVersion = Unit.INSTANCE;

    @Expose
    @ConfigOption(name = "Check for Updates", desc = "Automatically check for updates on startup")
    @ConfigEditorBoolean
    public Property<Boolean> checkForUpdates = Property.of(true);

    @Expose
    @ConfigOption(name = "Auto Updates", desc = "Automatically download new versions on startup")
    @ConfigEditorBoolean
    public boolean fullAutoUpdates = false;

    @Transient
    @ConfigOption(name = "Used Software", desc = "Information about used software and licenses")
    @Accordion
    public Licenses licenses = new Licenses();

    public static class Licenses {
        @Transient
        @ConfigOption(name = "MoulConfig", desc = "MoulConfig is available under the LGPL 3.0 License or later version")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable moulConfig = () -> openBrowser("https://github.com/NotEnoughUpdates/MoulConfig");

        @Transient
        @ConfigOption(name = "LibAutoUpdate", desc = "LibAutoUpdate is available under the BSD 2 Clause License")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable libAutoUpdate = () -> openBrowser("https://github.com/nea89o/libautoupdate");

        @Transient
        @ConfigOption(name = "Mixin", desc = "Mixin is available under the MIT License")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable mixin = () -> openBrowser("https://github.com/SpongePowered/Mixin/");

        @Transient
        @ConfigOption(name = "Guava", desc = "Guava is available under the Apache License 2.0")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable guava = () -> openBrowser("https://github.com/google/guava");

        @Transient
        @ConfigOption(name = "Orbit", desc = "Orbit is available under the MIT License")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable orbit = () -> openBrowser("https://github.com/MeteorDevelopment/orbit");

        @Transient
        @ConfigOption(name = "SkyHanni", desc = "SkyHanni is available under the LGPL 2.1 License")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable skyHanni = () -> openBrowser("https://github.com/hannibal002/SkyHanni");

        @Transient
        @ConfigOption(name = "Meteor Client", desc = "Meteor Client is available under the GPL-3.0 License")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable meteorClient = () -> openBrowser("https://github.com/MeteorDevelopment/meteor-client");

        private static void openBrowser(String url) {
            Util.getOperatingSystem().open(url);
        }
    }
}




