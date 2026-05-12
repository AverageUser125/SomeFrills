package com.somefrills.mixin.skyhanni;

import at.hannibal2.skyhanni.features.fishing.ThunderSparksHighlight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ThunderSparksHighlight.class)
public class ThunderSparkHighlightMixin {
    @ModifyVariable(method = "onRenderWorld", at = @At("STORE"), name = "seeThroughBlocks")
    private boolean modifyEnabled(boolean seeThroughBlocks) {
        return true;
    }
}
