package com.somefrills.config.misc;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class MiscCategory {
    @Expose
    @Accordion
    @ConfigOption(name = "Glow Player", desc = "Make players glow through walls")
    public GlowPlayerConfig glowPlayer = new GlowPlayerConfig();

    public static class GlowPlayerConfig {
        @Expose
        @ConfigOption(name = "Enabled", desc = "Make players glow through walls")
        @ConfigEditorBoolean
        public Property<Boolean> enabled = Property.of(true);
    }

    @Expose
    @Accordion
    @ConfigOption(name = "Mob Glow", desc = "Highlight entities based on name, type, or both")
    public MobGlowConfig glowMob = new MobGlowConfig();

    @Expose
    @Accordion
    @ConfigOption(name = "NPC Locator", desc = "Locate and track NPCs")
    public NpcLocatorConfig npcLocator = new NpcLocatorConfig();

    @Expose
    @Accordion
    @ConfigOption(name = "Command Aliases", desc = "Add aliases for commonly used commands")
    public CommandAliasesConfig commandAliases = new CommandAliasesConfig();

    public static class CommandAliasesConfig {
        @Expose
        @ConfigOption(name = "Enabled", desc = "Add aliases for commonly used commands")
        @ConfigEditorBoolean
        public Property<Boolean> enabled = Property.of(true);
    }

    @Expose
    @Accordion
    @ConfigOption(name = "Chat Filter", desc = "Filter out unwanted chat messages")
    public ChatFilterConfig chatFilter = new ChatFilterConfig();

    public static class ChatFilterConfig {
        @Expose
        @ConfigOption(name = "Enabled", desc = "Filter out unwanted chat messages")
        @ConfigEditorBoolean
        public Property<Boolean> enabled = Property.of(true);
    }

}