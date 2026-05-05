package com.somefrills.mixin.skyhanni;

import at.hannibal2.skyhanni.features.mining.glacitemineshaft.CorpseLocator;
import at.hannibal2.skyhanni.utils.HypixelCommands;
import com.somefrills.config.FrillsConfig;
import com.somefrills.features.mining.CorpseHighlight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    @Inject(method = "shareCorpse",
            at = @At("HEAD"),
            cancellable = true)
    private void onShareCorpse(CallbackInfo ci) {
        if (!FrillsConfig.instance.mining.corpseHighlight.forceSkyhanni) {
            return;
        }
        var messages = CorpseHighlight.shareAllWaypoints();
        for (String message : messages) {
            HypixelCommands.INSTANCE.partyChat(message, false);
        }
        ci.cancel();
    }
}

