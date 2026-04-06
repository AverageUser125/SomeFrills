package com.somefrills.config.solvers;

import io.github.notenoughupdates.moulconfig.ChromaColour;
import io.github.notenoughupdates.moulconfig.annotations.*;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class SolverCategory {

    @Accordion
    @ConfigOption(name = "Experiment Solver", desc = "Settings for the experiment solver")
    public ExperimentSolverConfig experimentSolver = new ExperimentSolverConfig();

    public static class ExperimentSolverConfig {
        @ConfigOption(name = "Enabled", desc = "Enable the experiment solver")
        @ConfigEditorBoolean
        public Property<Boolean> enabled = Property.of(true);

        @ConfigOption(name = "Click Delay", desc = "Click delay")
        @ConfigEditorSlider(minValue = 0, maxValue = 1000, minStep = 50)
        public int clickDelay = 400;

        @ConfigOption(name = "Chronomatron", desc = "Settings for the Chronomatron puzzle")
        @Accordion
        public ChronomatronSettings chronomatron = new ChronomatronSettings();

        public static class ChronomatronSettings {
            @ConfigOption(name = "Enable", desc = "Automatically solve the Chronomatron")
            @ConfigEditorBoolean
            public boolean enabled = true;

            @ConfigOption(name = "Close Chronomatron", desc = "Close menu at threshold")
            @ConfigEditorBoolean
            public boolean shouldClose = true;

            @ConfigOption(name = "Chronomatron Threshold", desc = "N means close after N-1 clicks")
            @ConfigEditorSlider(minValue = 1, maxValue = 50, minStep = 1)
            public int closeThreshold = 10;
        }

        @ConfigOption(name = "Ultrasequencer", desc = "Settings for the Ultrasequencer puzzle")
        @Accordion
        public UltrasequencerSettings ultrasequencer = new UltrasequencerSettings();

        public static class UltrasequencerSettings {
            @ConfigOption(name = "Enable", desc = "Automatically solve the Ultrasequencer")
            @ConfigEditorBoolean
            public boolean enabled = true;

            @ConfigOption(name = "Close Ultrasequencer", desc = "Close menu at threshold")
            @ConfigEditorBoolean
            public boolean shouldClose = true;

            @ConfigOption(name = "Ultrasequencer Threshold", desc = "N means close after N-1 clicks")
            @ConfigEditorSlider(minValue = 1, maxValue = 50, minStep = 1)
            public int closeThreshold = 7;
        }

        public SuperpairsSettings superpairs = new SuperpairsSettings();

        public static class SuperpairsSettings {
            @ConfigOption(name = "Enable", desc = "Enable superpairs helper")
            @ConfigEditorBoolean
            public boolean enabled = false;

            @ConfigOption(name = "Show revealed", desc = "Keep previously revealed pairs visible")
            @ConfigEditorBoolean
            public boolean keepRevealed = false;

            @ConfigOption(name = "Matched color", desc = "Color for matched pairs")
            @ConfigEditorColour
            public ChromaColour matchedColor = ChromaColour.fromStaticRGB(0, 255, 0, 128);

            @ConfigOption(name = "Matching Show", desc = "Color for matching pairs")
            @ConfigEditorColour
            public ChromaColour matchingColor = ChromaColour.fromStaticRGB(255, 255, 0, 128);

            @ConfigOption(name = "Powerup Show", desc = "Color for powerup pairs")
            @ConfigEditorColour
            public ChromaColour powerupColor = ChromaColour.fromStaticRGB(255, 0, 255, 128);

        }
    }

    @Accordion
    @ConfigOption(name = "Chocolate Factory Helper", desc = "Settings for the chocolate factory helper")
    public ChocolateFactoryConfig chocolateFactorySolver = new ChocolateFactoryConfig();

    public static class ChocolateFactoryConfig {
        @ConfigOption(name = "Enabled", desc = "Enable the chocolate factory helper")
        @ConfigEditorBoolean
        public Property<Boolean> enabled = Property.of(false);

        @ConfigOption(name = "Claim Stray Rabbits", desc = "Automatically claim stray rabbits in Chocolate Factory menu")
        @ConfigEditorBoolean
        public boolean claimStray = true;

        @ConfigOption(name = "Claim Delay", desc = "Delay between claim attempts in milliseconds")
        public int claimDelay = 100;

    }

}
