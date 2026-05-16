package com.somefrills.config

import com.google.gson.annotations.Expose
import com.somefrills.config.about.AboutCategory
import com.somefrills.config.farming.FarmingCategory
import com.somefrills.config.fishing.FishingCategory
import com.somefrills.config.mining.MiningCategory
import com.somefrills.config.misc.MiscCategory
import com.somefrills.config.solvers.SolverCategory
import com.somefrills.config.tweaks.TweaksCategory
import io.github.notenoughupdates.moulconfig.Config
import io.github.notenoughupdates.moulconfig.Social
import io.github.notenoughupdates.moulconfig.annotations.Category
import io.github.notenoughupdates.moulconfig.common.MyResourceLocation
import io.github.notenoughupdates.moulconfig.common.text.StructuredText

object FrillsConfig : Config() {

    private val socials: MutableList<Social> = mutableListOf(
        Social.forLink(
            StructuredText.of("Github"),
            MyResourceLocation("somefrills", "social/github.png"),
            "https://github.com/AverageUser125/SomeFrills"
        )
    )

    override fun getSocials() = socials

    override fun isValidRunnable(runnableId: Int) = false

    override fun getTitle() = StructuredText.of("Some Frills Config")

    @JvmField @Expose
    @Category(name = "About", desc = "Information and update settings")
    var about = AboutCategory()

    @JvmField @Expose
    @Category(name = "Solvers", desc = "Settings for puzzle solvers")
    var solvers = SolverCategory()

    @JvmField @Expose
    @Category(name = "Tweaks/Fixes", desc = "Settings for various tweaks and fixes")
    var tweaks = TweaksCategory()

    @JvmField @Expose
    @Category(name = "Farming", desc = "Settings for farming helpers")
    var farming = FarmingCategory()

    @JvmField @Expose
    @Category(name = "Mining", desc = "Settings for mining helpers")
    var mining = MiningCategory()

    @JvmField @Expose
    @Category(name = "Fishing", desc = "Settings for fishing helpers")
    var fishing = FishingCategory()

    @JvmField @Expose
    @Category(name = "Misc", desc = "Settings for miscellaneous features")
    var misc = MiscCategory()

    fun bind(config: FrillsConfig) {
        mining = config.mining
        farming = config.farming
        fishing = config.fishing
        misc = config.misc
        tweaks = config.tweaks
        solvers = config.solvers
        about = config.about
    }
}