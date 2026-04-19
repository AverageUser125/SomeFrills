package com.somefrills.config.mining;

import com.google.gson.annotations.Expose;
import com.somefrills.misc.RenderStyle;
import io.github.notenoughupdates.moulconfig.ChromaColour;
import io.github.notenoughupdates.moulconfig.annotations.*;
import io.github.notenoughupdates.moulconfig.observer.Property;

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
    @ConfigOption(name = "Corpse Highlightor", desc = "Settings for corpse highlighter in Mineshafts")
    public CorpseHighlightConfig corpseHighlight = new CorpseHighlightConfig();

    public static class CorpseHighlightConfig {
        @Expose
        @ConfigOption(name = "Enabled", desc = "Enable corpse highlighter in Mineshafts")
        @ConfigEditorBoolean
        public Property<Boolean> enabled = Property.of(true);

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
    @ConfigOption(name = "No Mining Trace", desc = "Allow mining through entities")
    @Accordion
    public NoMiningTraceConfig noMiningTrace = new NoMiningTraceConfig();

    public static class NoMiningTraceConfig {
        @Expose
        @ConfigOption(name = "No Mining Trace", desc = "Allow mining through entities")
        @ConfigEditorBoolean
        public Property<Boolean> enabled = Property.of(false);

        @Expose
        @ConfigOption(name = "Only When Holding Tool", desc = "Only allow mining through entities when holding a mining tool")
        @ConfigEditorBoolean
        public boolean onlyWhenHoldingTool = true;
    }
}
