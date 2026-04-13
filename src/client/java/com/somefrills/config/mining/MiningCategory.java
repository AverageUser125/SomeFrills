package com.somefrills.config.mining;

import com.google.gson.annotations.Expose;
import com.somefrills.misc.RenderStyle;
import io.github.notenoughupdates.moulconfig.ChromaColour;
import io.github.notenoughupdates.moulconfig.annotations.*;
import io.github.notenoughupdates.moulconfig.observer.Property;

import java.util.HashMap;

public class MiningCategory {
    @Expose
    @ConfigOption(name = "Gemstone Dsync Fix", desc = "Fix desync issues with gemstones in Dwarven Mines")
    @ConfigEditorBoolean
    public Property<Boolean> gemstoneDesyncFixEnabled = Property.of(false);

    @Expose
    @Accordion
    @ConfigOption(name = "Ghost Vision", desc = "Settings for ghost vision features in Dwarven Mines")
    public GhostVisionConfig ghostVision = new GhostVisionConfig();

    public static class GhostVisionConfig {
        @Expose
        @ConfigOption(name = "Enabled", desc = "Enable ghost vision features in Dwarven Mines")
        @ConfigEditorBoolean
        public Property<Boolean> enabled = Property.of(false);

        @Expose
        @ConfigOption(name = "Rendering Style", desc = "Rendering style for ghost boxes (fill/outline/both)")
        @ConfigEditorDropdown
        public RenderStyle style = RenderStyle.Both;

        @Expose
        @ConfigOption(name = "Fill Color", desc = "Fill color for ghost boxes")
        @ConfigEditorColour
        public ChromaColour fill = ChromaColour.fromStaticRGB(0, 200, 200, 128);

        @Expose
        @ConfigOption(name = "Outline Color", desc = "Outline color for ghost boxes")
        @ConfigEditorColour
        public ChromaColour outline = ChromaColour.fromStaticRGB(0, 200, 200, 255); // 0x00c8c8, 1.0f);

        @Expose
        @ConfigOption(name = "Remove Charge Visuals", desc = "Remove creeper 'charged' visuals")
        @ConfigEditorBoolean
        public boolean removeCharge = true;

        @Expose
        @ConfigOption(name = "Make Creepers Visible", desc = "Make creepers visible")
        @ConfigEditorBoolean
        public boolean makeCreepersVisible = false;

        @Expose
        @ConfigOption(name = "Ghosts show HP", desc = "Show HP of ghost creepers above their head")
        @ConfigEditorBoolean
        public boolean creeperShowHP = false;

        @Expose
        @ConfigOption(name = "Make All Creepers Visible", desc = "Make all creepers visible, even those that aren't ghosts")
        @ConfigEditorBoolean
        public boolean makeAllCreepersVisible = false;
    }

    @Expose
    @Accordion
    @ConfigOption(name = "Corpse Highlightor", desc = "Settings for corpse highlighter in Dwarven Mines")
    public CorpseHighlightConfig corpseHighlight = new CorpseHighlightConfig();

    public static class CorpseHighlightConfig {
        @Expose
        @ConfigOption(name = "Enabled", desc = "Enable corpse highlighter in Dwarven Mines")
        @ConfigEditorBoolean
        public Property<Boolean> enabled = Property.of(false);

        @Expose
        @ConfigOption(name = "Hide Opened Corpses", desc = "Hide glow on corpses you've already opened")
        @ConfigEditorBoolean
        public boolean hideOpened = true;

        @Expose
        @ConfigOption(name = "Lapis Glow Color", desc = "Glow color for Lapis Corpses")
        @ConfigEditorColour
        public ChromaColour lapisColor = ChromaColour.fromStaticRGB(85, 85, 255, 255);

        @Expose
        @ConfigOption(name = "Tungsten Glow Color", desc = "Glow color for Tungsten Corpses")
        @ConfigEditorColour
        public ChromaColour mineralColor = ChromaColour.fromStaticRGB(170, 170, 170, 255);

        @Expose
        @ConfigOption(name = "Umber Glow Color", desc = "Glow color for Umber Corpses")
        @ConfigEditorColour
        public ChromaColour yogColor = ChromaColour.fromStaticRGB(255, 170, 0, 255);

        @Expose
        @ConfigOption(name = "Vanguard Glow Color", desc = "Glow color for Vanguard Corpses")
        @ConfigEditorColour
        public ChromaColour vanguardColor = ChromaColour.fromStaticRGB(255, 85, 255, 255);
    }

    @Expose
    @Accordion
    @ConfigOption(name = "Ping Offset Miner", desc = "Settings for block-breaking timing optimization")
    public PingOffsetMinerConfig pingOffsetMiner = new PingOffsetMinerConfig();

    public static class PingOffsetMinerConfig {
        @Expose
        @ConfigOption(name = "Enabled", desc = "Enable Ping Offset Miner feature")
        @ConfigEditorBoolean
        public Property<Boolean> enabled = Property.of(false);

        @Expose
        @ConfigOption(name = "Active", desc = "Activate POM mining overlay")
        @ConfigEditorBoolean
        public boolean active = true;

        @Expose
        @ConfigOption(name = "Sound Enabled", desc = "Play sound when block is ready to break")
        @ConfigEditorBoolean
        public boolean sound = false;

        @Expose
        @ConfigOption(name = "Sound Path", desc = "Path to sound file (e.g., minecraft:block.stone.break)")
        @ConfigEditorText
        public String soundpath = "minecraft:block.stone.break";

        @Expose
        @ConfigOption(name = "Debug Mode", desc = "Enable debug mode with manual settings")
        @ConfigEditorBoolean
        public boolean debug = false;

        @Expose
        @ConfigOption(name = "Debug Speed", desc = "Manual mining speed when in debug mode")
        @ConfigEditorSlider(minValue = 0, maxValue = 2000, minStep = 10)
        public double speed = 100.0;

        @Expose
        @ConfigOption(name = "Debug Ping", desc = "Manual ping in ms when in debug mode")
        @ConfigEditorSlider(minValue = 0, maxValue = 1000, minStep = 10)
        public double ping = 50.0;

        @Expose
        @ConfigOption(name = "Extra Speed For Gems", desc = "Add extra speed bonus when breaking gems")
        @ConfigEditorBoolean
        public boolean extra = true;

        @Expose
        @ConfigOption(name = "Extra Speed Value", desc = "Amount of extra speed to add for gems")
        @ConfigEditorSlider(minValue = 0, maxValue = 2000, minStep = 10)
        public double extraVal = 855.0;

        @Expose
        @ConfigOption(name = "TPS Offset Multiplier", desc = "Multiplier for TPS deviation offset")
        @ConfigEditorSlider(minValue = 0, maxValue = 10, minStep = 0.1f)
        public double tpsOffsetMultiplier = 1.0;

        @Expose
        @ConfigOption(name = "Ping Offset Multiplier", desc = "Multiplier for ping offset")
        @ConfigEditorSlider(minValue = 0, maxValue = 10, minStep = 0.1f)
        public double pingOffsetMultiplier = 1.0;

        @Expose
        @ConfigOption(name = "Ability Tracking", desc = "Track mining speed ability cooldown")
        @ConfigEditorBoolean
        public boolean ability = true;

        @Expose
        @ConfigOption(name = "Logging", desc = "Enable debug logging")
        @ConfigEditorBoolean
        public boolean logging = false;

        @Expose
        @ConfigOption(name = "Show Warning", desc = "Show warning when mining speed not found")
        @ConfigEditorBoolean
        public boolean shouldWarn = true;

        @Expose
        @ConfigOption(name = "Block Line Rendering", desc = "Render block outlines")
        @Accordion
        public RenderSettingsConfig line = new RenderSettingsConfig(true);

        @Expose
        @ConfigOption(name = "Block Highlight Rendering", desc = "Render block highlights")
        @Accordion
        public RenderSettingsConfig highlight = new RenderSettingsConfig(true);

        @Expose
        public Property<HashMap<String, Boolean>> blockEnabled = Property.of(new HashMap<>());

        @Expose
        public Property<HashMap<String, Boolean>> islandEnabled = Property.of(new HashMap<>());
    }

    public static class RenderSettingsConfig {
        @Expose
        @ConfigOption(name = "Active", desc = "Enable this rendering style")
        @ConfigEditorBoolean
        public boolean active;

        @Expose
        @ConfigOption(name = "Color", desc = "Render color")
        @ConfigEditorColour
        public ChromaColour color = ChromaColour.fromStaticRGB(0, 200, 200, 128);

        public RenderSettingsConfig(boolean active) {
            this.active = active;
        }
    }
}
