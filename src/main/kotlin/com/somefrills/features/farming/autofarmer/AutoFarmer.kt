package com.somefrills.features.farming.autofarmer

import com.somefrills.Main
import com.somefrills.Main.mc
import com.somefrills.config.FrillsMod
import com.somefrills.events.ScreenOpenEvent
import com.somefrills.events.ServerJoinEvent
import com.somefrills.events.TickEventPost
import com.somefrills.features.core.AreaToggleFeature
import com.somefrills.features.core.FrillsFeature
import com.somefrills.misc.Area
import com.somefrills.misc.KeybindManager
import meteordevelopment.orbit.EventHandler
import net.minecraft.core.BlockPos

@FrillsFeature
object AutoFarmer :
    AreaToggleFeature(FrillsMod.config.farming.autoFarmer.enabled, FrillsMod.config.farming.autoFarmer.keybind) {
    val config get() = FrillsMod.config.farming.autoFarmer

    private var strategy: MovementStrategy
    private var currentState: MovementState
    private var savedState: MovementState? = null
    private var savedPosition: BlockPos? = null
    private var stateChangeSub: KeybindManager.Subscription? = null

    init {
        strategy = config.cropType.get().strategy
        currentState = strategy.getState()
    }

    private fun initStrategy() {
        strategy = config.cropType.get().strategy
        currentState = strategy.getState()
    }

    override fun checkArea(area: Area): Boolean {
        return area == Area.GARDEN
    }

    override fun onActivate() {
        Main.LOGGER.info("Activating AutoFarmer with strategy: ${config.cropType.get()}")
        initStrategy()
        registerKeyBindListener()

        // Early return if restoration conditions not met
        if (!config.restoreState || savedState == null || savedPosition == null || mc.player == null) {
            return
        }

        // Check distance if enabled
        if (config.enableDistanceCheck) {
            val maxDistance = config.maxRestoreDistance.toDouble()
            val distanceSquared = maxDistance * maxDistance
            val saved = savedPosition ?: return
            val actualDistance: Double = mc.player?.blockPosition()?.distSqr(saved) ?: Double.MAX_VALUE
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
        stateChangeSub = KeybindManager.register(config.stateChangeKeybind, Runnable {
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
        mc.options.keySprint.isDown = currentState.isSprinting
        mc.options.keyAttack.isDown = currentState.isAttacking
        mc.options.keyUp.isDown = currentState.isForward
        mc.options.keyDown.isDown = currentState.isBackward
        mc.options.keyLeft.isDown = currentState.isLeft
        mc.options.keyRight.isDown = currentState.isRight
    }

    @EventHandler
    fun onScreen(event: ScreenOpenEvent) {
        toggleActive()
    }

    private fun stopFarming() {
        mc.options.keySprint.isDown = false
        mc.options.keyAttack.isDown = false
        mc.options.keyUp.isDown = false
        mc.options.keyDown.isDown = false
        mc.options.keyLeft.isDown = false
        mc.options.keyRight.isDown = false
        savedState = currentState
        savedPosition = mc.player?.blockPosition()
    }

}