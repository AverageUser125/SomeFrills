package com.somefrills.mixin.skyhanni;

import at.hannibal2.skyhanni.features.mining.glacitemineshaft.CorpseLocator;
import com.somefrills.config.FrillsConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CorpseLocator.class)
public class CorpseLocatorMixin {
    // I can't figure out redirecting the method, so this will do
    @ModifyVariable(
            method = "findCorpse",
            at = @At("STORE"),
            name = "canSee")
    private boolean forceCanSeeTrue(boolean canSee) {
        if (FrillsConfig.instance.mining.corpseHighlight.forceSkyhanni) {
            return true; // Force canSee to be true
        }
        return canSee;
    }
}

