package com.somefrills.config.mining

import com.google.gson.annotations.Expose
import com.somefrills.misc.RenderStyle
import io.github.notenoughupdates.moulconfig.ChromaColour
import io.github.notenoughupdates.moulconfig.ChromaColour.Companion.fromStaticRGB
import io.github.notenoughupdates.moulconfig.annotations.*
import io.github.notenoughupdates.moulconfig.observer.Property

class MiningCategory {
    @Expose
    @ConfigOption(name = "Gemstone Dsync Fix", desc = "Fix desync issues with gemstones in Dwarven Mines")
    @ConfigEditorBoolean
    var gemstoneDesyncFixEnabled: Property<Boolean> = Property.of(false)

    @JvmField
    @Expose
    @Accordion
    @ConfigOption(name = "Ghost Vision", desc = "Settings for ghost vision features in Dwarven Mines")
    var ghostVision: GhostVisionConfig = GhostVisionConfig()

    class GhostVisionConfig {
        @JvmField
        @Expose
        @ConfigOption(name = "Enabled", desc = "Enable ghost vision features in Dwarven Mines")
        @ConfigEditorBoolean
        var enabled: Property<Boolean> = Property.of(false)

        @JvmField
        @Expose
        @ConfigOption(name = "Rendering Style", desc = "Rendering style for ghost boxes (fill/outline/both)")
        @ConfigEditorDropdown
        var style: RenderStyle = RenderStyle.Both

        @JvmField
        @Expose
        @ConfigOption(name = "Fill Color", desc = "Fill color for ghost boxes")
        @ConfigEditorColour
        var fill: ChromaColour = fromStaticRGB(0, 200, 200, 128)

        @JvmField
        @Expose
        @ConfigOption(name = "Outline Color", desc = "Outline color for ghost boxes")
        @ConfigEditorColour
        var outline: ChromaColour = fromStaticRGB(0, 200, 200, 255) // 0x00c8c8, 1.0f);

        @JvmField
        @Expose
        @ConfigOption(name = "Remove Charge Visuals", desc = "Remove creeper 'charged' visuals")
        @ConfigEditorBoolean
        var removeCharge: Boolean = true

        @JvmField
        @Expose
        @ConfigOption(name = "Make Creepers Visible", desc = "Make creepers visible")
        @ConfigEditorBoolean
        var makeCreepersVisible: Boolean = false

        @JvmField
        @Expose
        @ConfigOption(name = "Ghosts show HP", desc = "Show HP of ghost creepers above their head")
        @ConfigEditorBoolean
        var creeperShowHP: Boolean = false

        @JvmField
        @Expose
        @ConfigOption(
            name = "Make All Creepers Visible",
            desc = "Make all creepers visible, even those that aren't ghosts"
        )
        @ConfigEditorBoolean
        var makeAllCreepersVisible: Boolean = false
    }

    @JvmField
    @Expose
    @Accordion
    @ConfigOption(name = "Corpse Highlightor", desc = "Settings for corpse highlighter in Mineshafts")
    var corpseHighlight: CorpseHighlightConfig = CorpseHighlightConfig()

    class CorpseHighlightConfig {
        @JvmField
        @Expose
        @ConfigOption(name = "Enabled", desc = "Enable corpse highlighter in Mineshafts")
        @ConfigEditorBoolean
        var enabled: Property<Boolean> = Property.of(true)

        @Expose
        @ConfigOption(name = "Hide Opened Corpses", desc = "Hide glow on corpses you've already opened")
        @ConfigEditorBoolean
        var hideOpened: Boolean = true

        @JvmField
        @Expose
        @ConfigOption(name = "Lapis Glow Color", desc = "Glow color for Lapis Corpses")
        @ConfigEditorColour
        var lapisColor: ChromaColour = fromStaticRGB(85, 85, 255, 255)

        @JvmField
        @Expose
        @ConfigOption(name = "Tungsten Glow Color", desc = "Glow color for Tungsten Corpses")
        @ConfigEditorColour
        var mineralColor: ChromaColour = fromStaticRGB(170, 170, 170, 255)

        @JvmField
        @Expose
        @ConfigOption(name = "Umber Glow Color", desc = "Glow color for Umber Corpses")
        @ConfigEditorColour
        var yogColor: ChromaColour = fromStaticRGB(255, 170, 0, 255)

        @JvmField
        @Expose
        @ConfigOption(name = "Vanguard Glow Color", desc = "Glow color for Vanguard Corpses")
        @ConfigEditorColour
        var vanguardColor: ChromaColour = fromStaticRGB(255, 85, 255, 255)

        @JvmField
        @Expose
        @ConfigOption(name = "Force Skyhanni Waypoints", desc = "Force skyhanni corpse locator to run")
        @ConfigEditorBoolean
        var forceSkyhanni: Boolean = false
    }

    @JvmField
    @Expose
    @ConfigOption(name = "No Mining Trace", desc = "Allow mining through entities")
    @Accordion
    var noMiningTrace: NoMiningTraceConfig = NoMiningTraceConfig()

    class NoMiningTraceConfig {
        @JvmField
        @Expose
        @ConfigOption(name = "No Mining Trace", desc = "Allow mining through entities")
        @ConfigEditorBoolean
        var enabled: Property<Boolean> = Property.of(false)
    }
}
