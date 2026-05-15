package com.somefrills.features.farming.autofarmer;

import com.somefrills.config.FrillsConfig;
import com.somefrills.config.farming.AutoFarmerConfig;
import com.somefrills.events.ScreenOpenEvent;
import com.somefrills.events.ServerJoinEvent;
import com.somefrills.events.TickEventPost;
import com.somefrills.features.core.AreaToggleFeature;
import com.somefrills.misc.Area;
import com.somefrills.misc.KeybindManager;
import meteordevelopment.orbit.EventHandler;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static com.somefrills.Main.mc;

public class AutoFarmer extends AreaToggleFeature {
    private static AutoFarmerConfig config() {
        return FrillsConfig.instance.farming.autoFarmer;
    }

    @NonNull
    private MovementStrategy strategy;
    @NonNull
    private MovementState currentState;
    @Nullable
    private MovementState savedState;
    private KeybindManager.Subscription stateChangeSub = null;

    public AutoFarmer() {
        super(config().enabled, config().keybind);
        initStrategy();
    }

    private void initStrategy() {
        strategy = config().getCropType().getStrategy();
        currentState = strategy.getCurrentState();
    }

    @Override
    protected boolean checkArea(Area area) {
        return area.equals(Area.GARDEN);
    }

    @Override
    protected void onActivate() {
        initStrategy();
        // Restore saved state if it exists (from screen close), otherwise use strategy's current state
        if (savedState != null) {
            currentState = savedState;
            savedState = null;
        }

        stateChangeSub = KeybindManager.register(config().stateChangeKeybind, () -> {
            strategy.nextState();
            currentState = strategy.getCurrentState();
        });
    }

    @Override
    protected void onDeactivate() {
        stopFarming();
        stateChangeSub.unregister();
    }

    @EventHandler
    public void onServerSwitch(ServerJoinEvent event) {
        savedState = null;
    }

    @EventHandler
    public void onTick(TickEventPost event) {
        // Apply movement inputs
        mc.options.sprintKey.setPressed(currentState.isSprinting());
        mc.options.attackKey.setPressed(currentState.isAttacking());
        mc.options.forwardKey.setPressed(currentState.isForward());
        mc.options.backKey.setPressed(currentState.isBackward());
        mc.options.leftKey.setPressed(currentState.isLeft());
        mc.options.rightKey.setPressed(currentState.isRight());
    }

    @EventHandler
    public void onScreen(ScreenOpenEvent event) {
        toggleActive();
        stopFarming();
    }

    private void stopFarming() {
        mc.options.sprintKey.setPressed(false);
        mc.options.attackKey.setPressed(false);
        mc.options.forwardKey.setPressed(false);
        mc.options.backKey.setPressed(false);
        mc.options.leftKey.setPressed(false);
        mc.options.rightKey.setPressed(false);
        savedState = currentState;
    }
}
