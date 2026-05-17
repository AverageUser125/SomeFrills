package com.somefrills.config.solvers

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.ChromaColour
import io.github.notenoughupdates.moulconfig.ChromaColour.Companion.fromStaticRGB
import io.github.notenoughupdates.moulconfig.annotations.*
import io.github.notenoughupdates.moulconfig.observer.Property

class SolverCategory {
    @JvmField
    @Expose
    @Accordion
    @ConfigOption(name = "Experiment Solver", desc = "Settings for the experiment solver")
    var experimentSolver: ExperimentSolverConfig = ExperimentSolverConfig()

    class ExperimentSolverConfig {
        @JvmField
        @Expose
        @ConfigOption(name = "Enabled", desc = "Enable the experiment solver")
        @ConfigEditorBoolean
        var enabled: Property<Boolean> = Property.of(true)

        @JvmField
        @Expose
        @ConfigOption(name = "Click Delay", desc = "Click delay")
        @ConfigEditorSlider(minValue = 0f, maxValue = 1000f, minStep = 50f)
        var clickDelay: Int = 400

        @JvmField
        @Expose
        @ConfigOption(name = "Chronomatron", desc = "Settings for the Chronomatron puzzle")
        @Accordion
        var chronomatron: ChronomatronSettings = ChronomatronSettings()

        class ChronomatronSettings {
            @JvmField
            @Expose
            @ConfigOption(name = "Enable", desc = "Automatically solve the Chronomatron")
            @ConfigEditorBoolean
            var enabled: Boolean = true

            @JvmField
            @Expose
            @ConfigOption(name = "Close Chronomatron", desc = "Close menu at threshold")
            @ConfigEditorBoolean
            var shouldClose: Boolean = true

            @JvmField
            @Expose
            @ConfigOption(name = "Chronomatron Threshold", desc = "N means close after N-1 clicks")
            @ConfigEditorSlider(minValue = 1f, maxValue = 50f, minStep = 1f)
            var closeThreshold: Int = 10
        }

        @JvmField
        @Expose
        @ConfigOption(name = "Ultrasequencer", desc = "Settings for the Ultrasequencer puzzle")
        @Accordion
        var ultrasequencer: UltrasequencerSettings = UltrasequencerSettings()

        class UltrasequencerSettings {
            @JvmField
            @Expose
            @ConfigOption(name = "Enable", desc = "Automatically solve the Ultrasequencer")
            @ConfigEditorBoolean
            var enabled: Boolean = true

            @JvmField
            @Expose
            @ConfigOption(name = "Close Ultrasequencer", desc = "Close menu at threshold")
            @ConfigEditorBoolean
            var shouldClose: Boolean = true

            @JvmField
            @Expose
            @ConfigOption(name = "Ultrasequencer Threshold", desc = "N means close after N-1 clicks")
            @ConfigEditorSlider(minValue = 1f, maxValue = 50f, minStep = 1f)
            var closeThreshold: Int = 7
        }

        @Expose
        var superpairs: SuperpairsSettings = SuperpairsSettings()

        class SuperpairsSettings {
            @Expose
            @ConfigOption(name = "Enable", desc = "Enable superpairs helper")
            @ConfigEditorBoolean
            var enabled: Boolean = false

            @Expose
            @ConfigOption(name = "Show revealed", desc = "Keep previously revealed pairs visible")
            @ConfigEditorBoolean
            var keepRevealed: Boolean = false

            @Expose
            @ConfigOption(name = "Matched color", desc = "Color for matched pairs")
            @ConfigEditorColour
            var matchedColor: ChromaColour = fromStaticRGB(0, 255, 0, 128)

            @Expose
            @ConfigOption(name = "Matching Show", desc = "Color for matching pairs")
            @ConfigEditorColour
            var matchingColor: ChromaColour = fromStaticRGB(255, 255, 0, 128)

            @Expose
            @ConfigOption(name = "Powerup Show", desc = "Color for powerup pairs")
            @ConfigEditorColour
            var powerupColor: ChromaColour = fromStaticRGB(255, 0, 255, 128)
        }
    }

    @JvmField
    @Expose
    @Accordion
    @ConfigOption(name = "Chocolate Factory Helper", desc = "Settings for the chocolate factory helper")
    var chocolateFactorySolver: ChocolateFactoryConfig = ChocolateFactoryConfig()

    class ChocolateFactoryConfig {
        @JvmField
        @Expose
        @ConfigOption(name = "Enabled", desc = "Enable the chocolate factory helper")
        @ConfigEditorBoolean
        var enabled: Property<Boolean> = Property.of(false)

        @JvmField
        @Expose
        @ConfigOption(
            name = "Claim Stray Rabbits",
            desc = "Automatically claim stray rabbits in Chocolate Factory menu"
        )
        @ConfigEditorBoolean
        var claimStray: Boolean = true

        @JvmField
        @Expose
        @ConfigOption(name = "Claim Delay", desc = "Delay between claim attempts in milliseconds")
        var claimDelay: Int = 100
    }
}
