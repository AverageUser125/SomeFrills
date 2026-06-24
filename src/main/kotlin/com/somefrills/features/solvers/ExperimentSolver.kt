package com.somefrills.features.solvers

import com.somefrills.config.FrillsMod
import com.somefrills.features.core.AreaFeature
import com.somefrills.modules.FrillsFeature
import com.somefrills.misc.Area
import com.somefrills.utils.*


@FrillsFeature
object ExperimentSolver : AreaFeature(FrillsMod.config.solvers.experimentSolver.enabled) {
    private val config get() = FrillsMod.config.solvers.experimentSolver

    // </editor-fold>
    override fun checkArea(area: Area): Boolean {
        return area == Area.PRIVATE_ISLAND
    }
}