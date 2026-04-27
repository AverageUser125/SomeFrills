package com.somefrills.config;

import com.google.gson.annotations.Expose;
import com.somefrills.config.about.AboutCategory;
import com.somefrills.config.farming.FarmingCategory;
import com.somefrills.config.mining.MiningCategory;
import com.somefrills.config.misc.MiscCategory;
import com.somefrills.config.solvers.SolverCategory;
import com.somefrills.config.tweaks.TweaksCategory;
import io.github.notenoughupdates.moulconfig.Config;
import io.github.notenoughupdates.moulconfig.Social;
import io.github.notenoughupdates.moulconfig.annotations.Category;
import io.github.notenoughupdates.moulconfig.common.MyResourceLocation;
import io.github.notenoughupdates.moulconfig.common.text.StructuredText;

import java.util.List;

public class FrillsConfig extends Config {
    public static FrillsConfig instance = null;
    private final List<Social> socials = List.of(
            Social.forLink(StructuredText.of("Github"), new MyResourceLocation("somefrills", "social/github.png"), "https://github.com/AverageUser125/SomeFrills")
    );

    @Override
    public List<Social> getSocials() {
        return socials;
    }

    @Override
    public boolean isValidRunnable(int runnableId) {
        return false;
    }


    @Override
    public StructuredText getTitle() {
        return StructuredText.of("Some Frills Config");
    }

    @Expose
    @Category(name = "About", desc = "Information and update settings")
    public AboutCategory about = new AboutCategory();
    @Expose
    @Category(name = "Solvers", desc = "Settings for puzzle solvers")
    public SolverCategory solvers = new SolverCategory();
    @Expose
    @Category(name = "Tweaks/Fixes", desc = "Settings for various tweaks and fixes")
    public TweaksCategory tweaks = new TweaksCategory();
    @Expose
    @Category(name = "Farming", desc = "Settings for farming helpers")
    public FarmingCategory farming = new FarmingCategory();
    @Expose
    @Category(name = "Mining", desc = "Settings for mining helpers")
    public MiningCategory mining = new MiningCategory();
    @Expose
    @Category(name = "Misc", desc = "Settings for miscellaneous features")
    public MiscCategory misc = new MiscCategory();

}
