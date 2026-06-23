package com.somefrills.mixin;

import com.somefrills.events.ReceivePacketEvent;
import com.somefrills.events.SendPacketEvent;
import com.somefrills.events.ServerTickEvent;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.somefrills.Main.LOGGER;
import static com.somefrills.Main.eventBus;

@Mixin(Connection.class)
public abstract class ConnectionMixin {
    @Inject(method = "genericsFtw", at = @At("HEAD"), cancellable = true)
    private static void onPacketReceive(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
        if (packet instanceof ClientboundPingPacket pingPacket && pingPacket.getId() != 0) {
            eventBus.post(new ServerTickEvent());
        }
        if (eventBus.post(new ReceivePacketEvent(packet)).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V", at = @At("HEAD"), cancellable = true)
    private void onPacketSend(Packet<?> packet, @Nullable ChannelFutureListener listener, boolean flush, CallbackInfo ci) {
        if (packet instanceof ServerboundCustomPayloadPacket(CustomPacketPayload payload)) {
            CustomPacketPayload.Type<?> type = payload.type();
            String typeId = type.id().toString();
            if (typeId.contains("firmament")) {
                LOGGER.debug("Intercepted mod list packet, cancelling to prevent server from knowing about SomeFrills");
                ci.cancel();
            }
        }

        if (eventBus.post(new SendPacketEvent(packet)).isCancelled()) {
            ci.cancel();
        }
    }
}