package com.somefrills.config;

import com.somefrills.Main;
import com.somefrills.config.farming.FarmingCategory;
import com.somefrills.config.mining.MiningCategory;
import com.somefrills.config.misc.MiscCategory;
import com.somefrills.config.solvers.*;
import com.somefrills.config.tweaks.*;
import io.github.notenoughupdates.moulconfig.Config;
import io.github.notenoughupdates.moulconfig.annotations.Category;
import io.github.notenoughupdates.moulconfig.common.text.StructuredText;
import io.github.notenoughupdates.moulconfig.observer.Property;
import java.lang.reflect.Field;

public class FrillsConfig extends Config {
    public static FrillsConfig instance;
    @Override
    public boolean isValidRunnable(int runnableId) {
        return false;
    }

    @Override
    public StructuredText getTitle() {
        return StructuredText.of("Some Frills Config");
    }
    @Category(name = "Solvers", desc = "Settings for puzzle solvers")
    public SolverCategory solvers = new SolverCategory();
    @Category(name = "Tweaks/Fixes", desc = "Settings for various tweaks and fixes")
    public TweaksCategory tweaks = new TweaksCategory();
    @Category(name = "Farming", desc = "Settings for farming helpers")
    public FarmingCategory farming = new FarmingCategory();
    @Category(name = "Mining", desc = "Settings for mining helpers")
    public MiningCategory mining = new MiningCategory();
    @Category(name = "Misc", desc = "Settings for miscellaneous features")
    public MiscCategory misc = new MiscCategory();
}
