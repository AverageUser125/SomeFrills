package com.somefrills.features.farming.autofarmer;

import com.somefrills.config.FrillsConfig;
import com.somefrills.config.farming.AutoFarmerConfig;
import com.somefrills.events.ScreenOpenEvent;
import com.somefrills.events.ServerJoinEvent;
import com.somefrills.events.TickEventPost;
import com.somefrills.features.core.AreaToggleFeature;
import com.somefrills.features.core.FrillsFeature;
import com.somefrills.misc.Area;
import com.somefrills.misc.KeybindManager;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static com.somefrills.Main.mc;

@FrillsFeature
public class AutoFarmer extends AreaToggleFeature {
    private static AutoFarmerConfig config() {
        return FrillsConfig.farming.autoFarmer;
    }

    @NonNull
    private MovementStrategy strategy;
    @NonNull
    private MovementState currentState;
    @Nullable
    private MovementState savedState;
    @Nullable
    private BlockPos savedPosition;
    private KeybindManager.Subscription stateChangeSub = null;

    public AutoFarmer() {
        super(config().enabled, config().keybind);
        initStrategy();
    }

    private void initStrategy() {
        strategy = config().cropType.get().getStrategy();
        currentState = strategy.getCurrentState();
    }

    @Override
    protected boolean checkArea(Area area) {
        return area.equals(Area.GARDEN);
    }

    @Override
    protected void onActivate() {
        initStrategy();
        registerKeyBindListener();

        // Early return if restoration conditions not met
        if (!config().restoreState || savedState == null || savedPosition == null || mc.player == null) {
            return;
        }

        // Check distance if enabled
        if (config().enableDistanceCheck) {
            double maxDistance = config().maxRestoreDistance;
            double distanceSquared = maxDistance * maxDistance;
            double actualDistance = mc.player.getBlockPos().getSquaredDistance(savedPosition);
            if (actualDistance > distanceSquared) {
                // Out of range, don't restore
                return;
            }
        }

        // Restore state
        currentState = savedState;
        savedState = null;
        savedPosition = null;
    }

    private void registerKeyBindListener() {
        stateChangeSub = KeybindManager.register(config().stateChangeKeybind, () -> {
            strategy.nextState();
            currentState = strategy.getCurrentState();
        });
    }

    @Override
    protected void onDeactivate() {
        if (stateChangeSub != null) {
            stateChangeSub.unregister();
        }
        stopFarming();
    }

    @EventHandler
    public void onServerSwitch(ServerJoinEvent event) {
        savedState = null;
        savedPosition = null;
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
    }

    private void stopFarming() {
        mc.options.sprintKey.setPressed(false);
        mc.options.attackKey.setPressed(false);
        mc.options.forwardKey.setPressed(false);
        mc.options.backKey.setPressed(false);
        mc.options.leftKey.setPressed(false);
        mc.options.rightKey.setPressed(false);
        savedState = currentState;
        if (mc.player == null) return;
        savedPosition = mc.player.getBlockPos();
    }
}
