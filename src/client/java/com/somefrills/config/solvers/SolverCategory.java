package com.somefrills.config.solvers;

import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class SolverCategory {

    @Accordion
    @ConfigOption(name = "Experiment Solver", desc = "Settings for the experiment solver")
    public ExperimentSolverConfig experimentSolver = new ExperimentSolverConfig();
    public static class ExperimentSolverConfig {
            @ConfigOption(name = "Enabled", desc = "Enable the experiment solver")
            @ConfigEditorBoolean
            public Property<Boolean> enabled = Property.of(true);

            @ConfigOption(name = "Chronomatron", desc = "Automatically solve the Chronomatron")
            @ConfigEditorBoolean
            public boolean chronomatron = true;

            @ConfigOption(name = "Ultrasequencer", desc = "Automatically solve the Ultrasequencer")
            @ConfigEditorBoolean
            public boolean ultrasequencer = false;

            @ConfigOption(name = "Click Delay", desc = "Click delay")
            @ConfigEditorSlider(minValue = 0, maxValue = 1000, minStep = 50)
            public int clickDelay = 400;

            @ConfigOption(name = "Close Chronomatron", desc = "Close menu at threshold")
            @ConfigEditorBoolean
            public boolean closeOnChronomatronThreshold = true;

            @ConfigOption(name = "Chronomatron Threshold", desc = "N means close after N-1 clicks")
            @ConfigEditorSlider(minValue = 1, maxValue = 50, minStep = 1)
            public int chronomatronThreshold = 10;

            @ConfigOption(name = "Close Ultrasequencer", desc = "Close menu at threshold")
            @ConfigEditorBoolean
            public boolean closeOnUltrasequencerThreshold = true;

            @ConfigOption(name = "Ultrasequencer Threshold", desc = "N means close after N-1 clicks")
            @ConfigEditorSlider(minValue = 1, maxValue = 50, minStep = 1)
            public int ultrasequencerThreshold = 7;}


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
