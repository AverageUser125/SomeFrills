package com.somefrills.config.misc;

import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class MiscCategory {
    @Accordion
    @ConfigOption(name = "Auto Update", desc = "Automatically update the mod when a new version is available")
    public AutoUpdateConfig autoUpdate = new AutoUpdateConfig();
    public static class AutoUpdateConfig {
        @ConfigOption(name = "Enabled", desc = "Automatically update the mod when a new version is available")
        @ConfigEditorBoolean
        public Property<Boolean> enabled = Property.of(false);
    }

    @Accordion
    @ConfigOption(name = "Glow Player", desc = "Make players glow through walls")
    public GlowPlayerConfig glowPlayer = new GlowPlayerConfig();

    public static class GlowPlayerConfig {
        @ConfigOption(name = "Enabled", desc = "Make players glow through walls")
        @ConfigEditorBoolean
        public Property<Boolean> enabled = Property.of(true);
    }

    @Accordion
    @ConfigOption(name = "Command Aliases", desc = "Add aliases for commonly used commands")
    public CommandAliasesConfig commandAliases = new CommandAliasesConfig();
    public static class CommandAliasesConfig {
        @ConfigOption(name = "Enabled", desc = "Add aliases for commonly used commands")
        @ConfigEditorBoolean
        public Property<Boolean> enabled = Property.of(true);
    }

    @Accordion
    @ConfigOption(name = "Chat Filter", desc = "Filter out unwanted chat messages")
    public ChatFilterConfig chatFilter = new ChatFilterConfig();
    public static class ChatFilterConfig {
        @ConfigOption(name = "Enabled", desc = "Filter out unwanted chat messages")
        @ConfigEditorBoolean
        public Property<Boolean> enabled = Property.of(true);
    }
}