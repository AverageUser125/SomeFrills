package com.somefrills.features.farming.autofarmer;

import com.somefrills.config.FrillsConfig;
import com.somefrills.config.farming.AutoFarmerConfig;
import com.somefrills.events.ScreenOpenEvent;
import com.somefrills.events.TickEventPost;
import com.somefrills.features.core.AreaToggleFeature;
import com.somefrills.misc.Area;
import com.somefrills.misc.Input;
import meteordevelopment.orbit.EventHandler;
import org.lwjgl.glfw.GLFW;

import static com.somefrills.Main.mc;

public class AutoFarmer extends AreaToggleFeature {
    private static AutoFarmerConfig config() {
        return FrillsConfig.instance.farming.autoFarmer;
    }

    private MovementStrategy strategy = null;
    private MovementState currentState = MovementState.noMovement();
    private MovementState savedState = null;
    private boolean lastKeybindState = false;

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
    }

    @Override
    protected void onDeactivate() {
        stopFarming();
    }

    @EventHandler
    public void onTick(TickEventPost event) {
        if (strategy == null) return;

        // Check if the state change keybind is pressed

        boolean isKeyPressed = Input.isKeyPressed(config().stateChangeKeybind.get());
        // Detect key press (transition from not pressed to pressed)
        if (isKeyPressed && !lastKeybindState) {
            strategy.nextState();
            currentState = strategy.getCurrentState();
        }
        lastKeybindState = isKeyPressed;

        // Apply movement inputs
        mc.options.sprintKey.setPressed(false);
        mc.options.attackKey.setPressed(true); // Always attack
        mc.options.forwardKey.setPressed(currentState.forward);
        mc.options.backKey.setPressed(currentState.backward);
        mc.options.leftKey.setPressed(currentState.left);
        mc.options.rightKey.setPressed(currentState.right);
    }

    @EventHandler
    public void onScreen(ScreenOpenEvent event) {
        savedState = currentState;
        stopFarming();
    }

    private void stopFarming() {
        mc.options.sprintKey.setPressed(false);
        mc.options.attackKey.setPressed(false);
        mc.options.forwardKey.setPressed(false);
        mc.options.backKey.setPressed(false);
        mc.options.leftKey.setPressed(false);
        mc.options.rightKey.setPressed(false);
        currentState = MovementState.noMovement();
        toggleActive();
    }
}
