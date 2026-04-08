package com.somefrills.config.about;

import com.somefrills.features.update.AutoUpdate;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;
import kotlin.jvm.Transient;
import net.minecraft.util.Unit;

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

    @Transient
    @ConfigOption(name = "Update Immediately", desc = "Starts a manual update check")
    @ConfigEditorButton(buttonText = "Check Now")
    public Runnable checkForUpdatesButton = AutoUpdate::checkUpdate;
}



