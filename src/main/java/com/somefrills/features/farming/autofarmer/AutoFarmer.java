package com.somefrills.features.farming.autofarmer;

import com.somefrills.config.FrillsConfig;
import com.somefrills.config.farming.AutoFarmerConfig;
import com.somefrills.events.ScreenOpenEvent;
import com.somefrills.events.TickEventPost;
import com.somefrills.features.core.AreaToggleFeature;
import com.somefrills.misc.Area;
import meteordevelopment.orbit.EventHandler;

import static com.somefrills.Main.mc;

public class AutoFarmer extends AreaToggleFeature {
    private static AutoFarmerConfig config() {
        return FrillsConfig.instance.farming.autoFarmer;
    }

    private MovementStrategy strategy;

    public AutoFarmer() {
        super(config().enabled, config().keybind);
        initStrategy();
    }

    private void initStrategy() {
        CropType cropType = config().getCropType();
        strategy = switch (cropType.getPattern()) {
            case RECTANGULAR -> new RectangularMovement();
            case DIAGONAL -> new DiagonalMovement();
            case MUSHROOM_FORWARD -> new MushroomMovement();
        };
    }

    @Override
    protected boolean checkArea(Area area) {
        return area.equals(Area.GARDEN);
    }

    @Override
    protected void onActivate() {
        initStrategy();
        // Listen for crop type changes to re-initialize strategy
        config().cropType.addObserver((o, n) -> initStrategy());
        if (strategy != null) {
            strategy.reset();
        }
    }

    @Override
    protected void onDeactivate() {
        stopFarming();
    }

    @EventHandler
    public void onTick(TickEventPost event) {
        if (strategy == null) return;

        MovementStrategy.MovementInput input = strategy.getMovement();

        // Apply movement inputs
        mc.options.attackKey.setPressed(input.attack);
        mc.options.forwardKey.setPressed(input.forward);
        mc.options.backKey.setPressed(input.backward);
        mc.options.leftKey.setPressed(input.left);
        mc.options.rightKey.setPressed(input.right);
    }

    @EventHandler
    public void onScreen(ScreenOpenEvent event) {
        stopFarming();
    }

    private void stopFarming() {
        mc.options.attackKey.setPressed(false);
        mc.options.forwardKey.setPressed(false);
        mc.options.backKey.setPressed(false);
        mc.options.leftKey.setPressed(false);
        mc.options.rightKey.setPressed(false);
        if (strategy != null) {
            strategy.reset();
        }
    }
}
