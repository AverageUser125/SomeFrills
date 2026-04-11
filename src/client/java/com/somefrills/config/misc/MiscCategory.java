package com.somefrills.config.misc;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.ChromaColour;
import io.github.notenoughupdates.moulconfig.annotations.*;
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

    public static class MobGlowConfig {
        @Expose
        @ConfigOption(name = "Enabled", desc = "Highlight entities based on name, type, or both")
        @ConfigEditorBoolean
        public Property<Boolean> enabled = Property.of(false);
    }

    @Expose
    @Accordion
    @ConfigOption(name = "NPC Locator", desc = "Locate and track NPCs")
    public NpcLocatorConfig npcLocator = new NpcLocatorConfig();

    public static class NpcLocatorConfig {
        @Expose
        @ConfigOption(name = "Enabled", desc = "Locate and track NPCs")
        @ConfigEditorBoolean
        public Property<Boolean> enabled = Property.of(false);

        @Expose
        @ConfigOption(name = "Beacon Beam", desc = "Show beacon beam to NPCs")
        @ConfigEditorBoolean
        public boolean beaconBeam = false;

        @Expose
        @ConfigOption(name = "Tracer", desc = "Show tracer lines to NPCs")
        @ConfigEditorBoolean
        public boolean tracer = true;

        @Expose
        @ConfigOption(name = "Outline Box", desc = "Show outline box around NPCs")
        @ConfigEditorBoolean
        public boolean outlineBox = false;

        @Expose
        @ConfigOption(name = "Color", desc = "Rendering color for NPC locator")
        @ConfigEditorColour
        public Property<ChromaColour> color = Property.of(ChromaColour.fromStaticRGB(255, 100, 100, 255));

        @Expose
        @ConfigOption(name = "Auto Remove Waypoint", desc = "Automatically remove NPC waypoints when you get close to them")
        @ConfigEditorBoolean
        public boolean autoRemoveWaypoint;

        @Expose
        @ConfigOption(name = "Waypoint Remove Distance", desc = "Distance threshold for automatically removing NPC waypoints")
        @ConfigEditorSlider(minValue = 1.0f, maxValue = 20.0f, minStep = 0.5f)
        public float waypointRemoveDistance = 6.0f;
    }

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

    @Expose
    @Accordion
    @ConfigOption(name = "DA Player Worth", desc = "Show the worth of players while in dark auction")
    public DAPlayerWorthConfig daPlayerWorth = new DAPlayerWorthConfig();

    public static class DAPlayerWorthConfig {
        @Expose
        @ConfigOption(name = "Enabled", desc = "Show the worth of players while in dark auction")
        @ConfigEditorBoolean
        public Property<Boolean> enabled = Property.of(true);
    }
}