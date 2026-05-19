package com.somefrills.mixin.skyhanni;

import at.hannibal2.skyhanni.deps.moulconfig.ChromaColour;
import at.hannibal2.skyhanni.features.inventory.experimentationtable.ExperimentsAddonsHelper;
import com.llamalad7.mixinextras.sugar.Local;
import com.somefrills.features.solvers.ExperimentSolver;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(ExperimentsAddonsHelper.class)
public class ExperimentsAddonsHelperMixin {
    @Inject(
            method = "tryHighlightChronomatron",
            at = @At(value = "INVOKE", target = "Lat/hannibal2/skyhanni/utils/RenderUtils;highlight(Lnet/minecraft/screen/slot/Slot;Ljava/awt/Color;)V")
    )
    private void onChronomatronHighlight(CallbackInfo ci, @Local Slot slot, @Local ChromaColour slotColor) {
        ExperimentSolver.onChronomatronHighlight(slot, slotColor.getEffectiveColour());
    }

    @Inject(
            method = "tryHighlightUltrasequencer",
            at = @At(value = "INVOKE", target = "Lat/hannibal2/skyhanni/utils/RenderUtils;highlight(Lnet/minecraft/screen/slot/Slot;Ljava/awt/Color;)V")
    )
    private void onUltrasequencerHighlight(CallbackInfo ci, @Local Slot slot, @Local Color slotColor) {
        ExperimentSolver.onUltrasequencerHighlight(slot, slotColor);
    }
}
