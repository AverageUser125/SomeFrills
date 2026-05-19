package com.somefrills.features.solvers

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.utils.DelayedRun
import com.somefrills.Main
import com.somefrills.config.FrillsMod
import com.somefrills.features.core.Feature
import com.somefrills.features.core.FrillsFeature
import com.somefrills.utils.ContainerUtils
import net.minecraft.screen.slot.Slot
import java.awt.Color
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration


@FrillsFeature
object ExperimentSolver : Feature(FrillsMod.config.solvers.experimentSolver.enabled) {
    private val config get() = FrillsMod.config.solvers.experimentSolver
    private var inFlightClick = false

    @JvmStatic
    fun onChronomatronHighlight(slot: Slot, color: Color) {
        if (!config.chronomatron.enabled) return
        if (!isValidChronoSlot(slot.index)) return
        if (!isNextColor(color)) return
        if (inFlightClick) return
        Main.LOGGER.info("tryHighlightChronomatron called, slot: {}", slot)
        inFlightClick = true
        DelayedRun.runDelayed(config.clickDelay.toDuration(DurationUnit.MILLISECONDS)) {
            Main.LOGGER.info("Clicking Chronomatron slot: {}", slot)
            ContainerUtils.clickSlot(slot)
            inFlightClick = false
        }
    }

    @JvmStatic
    fun onUltrasequencerHighlight(slot: Slot, color: Color) {
        if (!config.ultrasequencer.enabled) return
        if (!isNextColor(color)) return
        if (inFlightClick) return
        Main.LOGGER.info("tryHighlightUltrasequencer called, slot: {}", slot)
        inFlightClick = true
        DelayedRun.runDelayed(config.clickDelay.toDuration(DurationUnit.MILLISECONDS)) {
            Main.LOGGER.info("Clicking Ultrasequencer slot: {}", slot)
            ContainerUtils.clickSlot(slot)
            inFlightClick = false
        }
    }

    @JvmStatic
    fun isNextColor(color: Color): Boolean {
        val nextColor: Color = SkyHanniMod.feature.inventory.experimentationTable.addons.nextColor.getEffectiveColour()
        return nextColor.rgb == color.rgb
    }

    private fun isValidChronoSlot(idx: Int): Boolean {
        return (idx in 11..19) || (idx in 30..38)
    }
}