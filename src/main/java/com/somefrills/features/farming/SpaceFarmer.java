package com.somefrills.features.farming;

import com.somefrills.config.FrillsConfig;
import com.somefrills.config.farming.FarmingCategory.SpaceFarmerConfig;
import com.somefrills.events.ScreenOpenEvent;
import com.somefrills.events.TickEventPost;
import com.somefrills.features.core.ToggleFeature;
import meteordevelopment.orbit.EventHandler;

import static com.somefrills.Main.mc;

public class SpaceFarmer extends ToggleFeature {
    private static SpaceFarmerConfig config() {
        return FrillsConfig.instance.farming.spaceFarmer;
    }

    public SpaceFarmer() {
        super(config().enabled, config().keybind);
    }

    @Override
    protected void onDeactivate() {
        mc.options.attackKey.setPressed(false);
        if(config().forwardKey) mc.options.forwardKey.setPressed(false);
    }

    @EventHandler
    public void onKey(TickEventPost event) {
        mc.options.attackKey.setPressed(true);
        if(config().forwardKey) mc.options.forwardKey.setPressed(true);
    }

    @EventHandler
    public void onScreen(ScreenOpenEvent event) {
        toggle();
    }
}
