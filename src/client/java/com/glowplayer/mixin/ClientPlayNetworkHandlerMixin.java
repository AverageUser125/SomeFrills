package com.glowplayer.mixin;


import com.glowplayer.features.UpdateChecker;
import com.glowplayer.tweaks.BreakResetFix;
import com.glowplayer.utils.AllConfig;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

import static com.glowplayer.Main.mc;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onScreenHandlerSlotUpdate", at = @At("TAIL"))
    private void onUpdateInventory(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo ci) {
        if (mc.currentScreen == null) {
            BreakResetFix.onInventoryUpdate(packet, packet.getStack(), packet.getSlot());
        }
    }

    @Inject(method = "onEntityTrackerUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/data/DataTracker;writeUpdatedEntries(Ljava/util/List;)V"))
    private void onPreTrackerUpdate(EntityTrackerUpdateS2CPacket packet, CallbackInfo ci, @Local Entity ent) {
        if (ent.equals(mc.player) && AllConfig.animationsFix) {
            for (DataTracker.SerializedEntry<?> entry : new ArrayList<>(packet.trackedValues())) {
                if (entry.handler().equals(TrackedDataHandlerRegistry.ENTITY_POSE)) {
                    packet.trackedValues().remove(entry);
                    break;
                }
            }
        }
    }
    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void onJoinGame(GameJoinS2CPacket packet, CallbackInfo ci) {
        UpdateChecker.checkUpdate();
    }
}