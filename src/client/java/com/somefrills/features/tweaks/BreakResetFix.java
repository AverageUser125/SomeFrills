package com.somefrills.features.tweaks;

import com.somefrills.config.FrillsConfig;
import com.somefrills.events.InventoryUpdateEvent;
import com.somefrills.features.core.Feature;
import com.somefrills.mixin.ClientPlayerInteractionManagerAccessor;
import meteordevelopment.orbit.EventHandler;

import static com.somefrills.Main.mc;

public class BreakResetFix extends Feature {

    public BreakResetFix() {
        super(FrillsConfig.instance.tweaks.breakResetFixEnabled);
    }

    @EventHandler
    public void onBreakReset(InventoryUpdateEvent event) {
        if (mc.player != null && mc.interactionManager != null) {
            if (event.slotId >= 36 && event.slotId <= 44 && mc.player.getInventory().getSelectedSlot() == event.slotId - 36) {
                ((ClientPlayerInteractionManagerAccessor) mc.interactionManager).setStack(event.stack);
            } // manually update the variable once the server updates our held item, prevents the mismatch and thus fixes the break cancel
        }

    }
}
