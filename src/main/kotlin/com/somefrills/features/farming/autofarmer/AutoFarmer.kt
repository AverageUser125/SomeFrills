package com.somefrills.features.farming.autofarmer

import com.somefrills.Main.mc
import com.somefrills.config.FrillsConfig
import com.somefrills.config.farming.AutoFarmerConfig
import com.somefrills.events.ScreenOpenEvent
import com.somefrills.events.ServerJoinEvent
import com.somefrills.events.TickEventPost
import com.somefrills.features.core.AreaToggleFeature
import com.somefrills.features.core.FrillsFeature
import com.somefrills.misc.Area
import com.somefrills.misc.KeybindManager
import meteordevelopment.orbit.EventHandler
import net.minecraft.util.math.BlockPos

@FrillsFeature
class AutoFarmer : AreaToggleFeature(config().enabled, config().keybind) {
    private var strategy: MovementStrategy
    private var currentState: MovementState
    private var savedState: MovementState? = null
    private var savedPosition: BlockPos? = null
    private var stateChangeSub: KeybindManager.Subscription? = null

    init {
        strategy = config().cropType.get().strategy
        currentState = strategy.getState()
    }

    private fun initStrategy() {
        strategy = config().cropType.get().strategy
        currentState = strategy.getState()
    }

    override fun checkArea(area: Area): Boolean {
        return area == Area.GARDEN
    }

    override fun onActivate() {
        initStrategy()
        registerKeyBindListener()

        // Early return if restoration conditions not met
        if (!config().restoreState || savedState == null || savedPosition == null || mc.player == null) {
            return
        }

        // Check distance if enabled
        if (config().enableDistanceCheck) {
            val maxDistance = config().maxRestoreDistance.toDouble()
            val distanceSquared = maxDistance * maxDistance
            val actualDistance: Double = mc.player?.blockPos?.getSquaredDistance(savedPosition) ?: Double.MAX_VALUE
            if (actualDistance > distanceSquared) {
                // Out of range, don't restore
                return
            }
        }

        // Restore state
        currentState = savedState!!
        savedState = null
        savedPosition = null
    }

    private fun registerKeyBindListener() {
        stateChangeSub = KeybindManager.register(config().stateChangeKeybind, Runnable {
            strategy.nextState()
            currentState = strategy.getState()
        })
    }

    override fun onDeactivate() {
        if (stateChangeSub != null) {
            stateChangeSub!!.unregister()
        }
        stopFarming()
    }

    @EventHandler
    fun onServerSwitch(event: ServerJoinEvent) {
        savedState = null
        savedPosition = null
    }

    @EventHandler
    fun onTick(event: TickEventPost?) {
        // Apply movement inputs
        mc.options.sprintKey.isPressed = currentState.isSprinting
        mc.options.attackKey.isPressed = currentState.isAttacking
        mc.options.forwardKey.isPressed = currentState.isForward
        mc.options.backKey.isPressed = currentState.isBackward
        mc.options.leftKey.isPressed = currentState.isLeft
        mc.options.rightKey.isPressed = currentState.isRight
    }

    @EventHandler
    fun onScreen(event: ScreenOpenEvent) {
        toggleActive()
    }

    private fun stopFarming() {
        mc.options.sprintKey.isPressed = false
        mc.options.attackKey.isPressed = false
        mc.options.forwardKey.isPressed = false
        mc.options.backKey.isPressed = false
        mc.options.leftKey.isPressed = false
        mc.options.rightKey.isPressed = false
        savedState = currentState
        savedPosition = mc.player?.blockPos
    }

    companion object {
        private fun config(): AutoFarmerConfig {
            return FrillsConfig.farming.autoFarmer
        }
    }
}