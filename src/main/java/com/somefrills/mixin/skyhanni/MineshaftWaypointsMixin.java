package com.somefrills.mixin.skyhanni;

import at.hannibal2.skyhanni.config.features.mining.glacite.GlaciteMineshaftConfig;
import at.hannibal2.skyhanni.data.PartyApi;
import at.hannibal2.skyhanni.events.minecraft.KeyPressEvent;
import at.hannibal2.skyhanni.features.mining.glacitemineshaft.MineshaftWaypoints;
import at.hannibal2.skyhanni.utils.HypixelCommands;
import com.somefrills.config.FrillsConfig;
import com.somefrills.features.mining.CorpseHighlight;
import com.somefrills.misc.Area;
import com.somefrills.misc.Utils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(MineshaftWaypoints.class)
public abstract class MineshaftWaypointsMixin {
    @Shadow
    protected abstract GlaciteMineshaftConfig getConfig();

    @Inject(method = "onKeyPress", at = @At("HEAD"), cancellable = true)
    private void onKeyPress(KeyPressEvent event, CallbackInfo ci) {
        if (!FrillsConfig.instance.mining.corpseHighlight.forceSkyhanni) {
            return;
        }
        if (!Utils.isInArea(Area.MINESHAFT)) return;
        if (event.getKeyCode() != getConfig().getShareWaypointLocation()) return;

        var messages = CorpseHighlight.shareAllWaypointsForce();
        for(var message : messages) {
            if (!PartyApi.INSTANCE.getPartyMembers().isEmpty()) {
                HypixelCommands.INSTANCE.partyChat(message, false);
            } else {
                HypixelCommands.INSTANCE.allChat(message);
            }
        }

        ci.cancel();
    }
}
