package com.somefrills.config.mining;

import com.somefrills.misc.RenderStyle;
import io.github.notenoughupdates.moulconfig.ChromaColour;
import io.github.notenoughupdates.moulconfig.annotations.*;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class MiningCategory {
    @ConfigOption(name = "Gemstone Dsync Fix", desc = "Fix desync issues with gemstones in Dwarven Mines")
    @ConfigEditorBoolean
    public Property<Boolean> gemstoneDesyncFixEnabled = Property.of(false);

    @Accordion
    @ConfigOption(name = "Ghost Vision", desc = "Settings for ghost vision features in Dwarven Mines")
    public GhostVisionConfig ghostVision = new GhostVisionConfig();
    public static class GhostVisionConfig {
        @ConfigOption(name = "Enabled", desc = "Enable ghost vision features in Dwarven Mines")
        @ConfigEditorBoolean
        public Property<Boolean> enabled = Property.of(false);

        @ConfigOption(name = "Rendering Style", desc = "Rendering style for ghost boxes (fill/outline/both)")
        @ConfigEditorDropdown
        public RenderStyle style = RenderStyle.Both;

        @ConfigOption(name = "Fill Color", desc = "Fill color for ghost boxes")
        @ConfigEditorColour
        public ChromaColour fill = ChromaColour.fromStaticRGB(0, 200, 200, 128);

        @ConfigOption(name = "Outline Color", desc = "Outline color for ghost boxes")
        @ConfigEditorColour
        public ChromaColour outline = ChromaColour.fromStaticRGB(0, 200, 200, 255); // 0x00c8c8, 1.0f);

        @ConfigOption(name = "Remove Charge Visuals", desc = "Remove creeper 'charged' visuals")
        @ConfigEditorBoolean
        public boolean removeCharge = true;

        @ConfigOption(name = "Make Creepers Visible", desc = "Make creepers visible")
        @ConfigEditorBoolean
        public boolean makeCreepersVisible = false;

        @ConfigOption(name = "Ghosts show HP", desc = "Show HP of ghost creepers above their head")
        @ConfigEditorBoolean
        public boolean creeperShowHP = false;
    }

    @Accordion
    @ConfigOption(name = "Corpse Highlightor", desc = "Settings for corpse highlighter in Dwarven Mines")
    public CorpseHighlightConfig corpseHighlight = new CorpseHighlightConfig();
    public static class CorpseHighlightConfig {
        @ConfigOption(name = "Enabled", desc = "Enable corpse highlighter in Dwarven Mines")
        @ConfigEditorBoolean
        public Property<Boolean> enabled = Property.of(false);

        @ConfigOption(name = "Hide Opened Corpses", desc = "Hide glow on corpses you've already opened")
        @ConfigEditorBoolean
        public boolean hideOpened = true;

        @ConfigOption(name = "Lapis Glow Color", desc = "Glow color for Lapis Corpses")
        @ConfigEditorColour
        public ChromaColour lapisColor = ChromaColour.fromStaticRGB(85, 85, 255, 255);

        @ConfigOption(name = "Tungsten Glow Color", desc = "Glow color for Tungsten Corpses")
        @ConfigEditorColour
        public ChromaColour mineralColor = ChromaColour.fromStaticRGB(170, 170, 170, 255);

        @ConfigOption(name = "Umber Glow Color", desc = "Glow color for Umber Corpses")
        @ConfigEditorColour
        public ChromaColour yogColor = ChromaColour.fromStaticRGB(255, 170, 0, 255);

        @ConfigOption(name = "Vanguard Glow Color", desc = "Glow color for Vanguard Corpses")
        @ConfigEditorColour
        public ChromaColour vanguardColor = ChromaColour.fromStaticRGB(255, 85, 255, 255);
    }
}
