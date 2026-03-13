package com.example.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ConnectionMixin {

    @Unique
    private static final Logger glowplayer$LOGGER = LoggerFactory.getLogger("GlowPlayer");

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void glowplayer$interceptPackets(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof CustomPayloadC2SPacket customPayloadPacket) {
            CustomPayload payload = customPayloadPacket.payload();
            CustomPayload.Id<?> type = payload.getId();
            if(type == null) return;
            String typeId = type.id().toString();
            if (typeId.equals("firmament:mod_list")) {
                glowplayer$LOGGER.info("Blocked Firmament mod announcer packet (evil)");
                ci.cancel();
            }
        }
    }
}
