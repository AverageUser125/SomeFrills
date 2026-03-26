// TODO(Ravel): Failed to fully resolve file: null cannot be cast to non-null type com.intellij.psi.PsiClass
package com.somefrills.features.tweaks;

import com.somefrills.config.Feature;
import com.somefrills.events.InventoryUpdateEvent;
import com.somefrills.mixin.ClientPlayerInteractionManagerAccessor;
import meteordevelopment.orbit.EventHandler;

import static com.somefrills.Main.mc;

public class BreakResetFix {
    public static final Feature instance = new Feature("breakResetFix", true);

    @EventHandler
    public static void onBreakReset(InventoryUpdateEvent event) {
        if (!instance.isActive()) return;
        if (mc.player != null && mc.interactionManager != null) {
            if (event.slotId >= 36 && event.slotId <= 44 && mc.player.getInventory().getSelectedSlot() == event.slotId - 36) {
                ((ClientPlayerInteractionManagerAccessor) mc.interactionManager).setStack(event.stack);
            } // manually update the variable once the server updates our held item, prevents the mismatch and thus fixes the break cancel
        }

    }
}
