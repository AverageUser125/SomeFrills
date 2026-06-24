package com.somefrills.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.somefrills.events.*;
import com.somefrills.misc.SkyblockData;

import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

import static com.somefrills.Main.mc;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(
            method = "handleSetEntityData",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/syncher/SynchedEntityData;assignValues(Ljava/util/List;)V",
                    shift = At.Shift.AFTER
            )
    )

    @SuppressWarnings("unchecked")
    private void onPostTrackerUpdate(
            ClientboundSetEntityDataPacket packet,
            CallbackInfo ci,
            @Local Entity ent
    ) {
        if (ent instanceof LivingEntity || ent instanceof ItemEntity) {

            if (ent instanceof ArmorStand) {
                for (SynchedEntityData.DataValue<?> entry : packet.packedItems()) {

                    if (entry.serializer().equals(EntityDataSerializers.OPTIONAL_COMPONENT)
                            && entry.value() != null) {

                        //noinspection unchecked
                        ((Optional<Component>) entry.value())
                                .ifPresent(value ->
                                        new EntityNamedEvent(ent, value).post()
                                );

                        break;
                    }
                }
            }

            new EntityUpdatedEvent(ent).post();
        }
    }


    @Inject(
            method = "handleAddEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;postAddEntitySoundInstance(Lnet/minecraft/world/entity/Entity;)V"
            )
    )
    private void onEntitySpawn(
            ClientboundAddEntityPacket packet,
            CallbackInfo ci,
            @Local Entity ent
    ) {
        new EntityUpdatedEvent(ent).post();
    }


    @Inject(
            method = "handleContainerSetSlot",
            at = @At("TAIL")
    )
    private void onUpdateInventory(
            ClientboundContainerSetSlotPacket packet,
            CallbackInfo ci
    ) {

        if (mc.screen instanceof ContainerScreen container) {

            new SlotUpdateEvent(
                    packet,
                    container,
                    container.getMenu(),
                    container.getMenu().getContainer(),
                    container.getMenu().getSlot(packet.getSlot()),
                    packet.getItem()
            ).post();

        } else if (mc.screen == null) {

            new InventoryUpdateEvent(
                    packet,
                    packet.getItem(),
                    packet.getSlot()
            ).post();
        }
    }


    @Inject(
            method = "handleParticleEvent",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void onParticle(
            ClientboundLevelParticlesPacket packet,
            CallbackInfo ci
    ) {
        if ((new SpawnParticleEvent(packet)).post().isCancelled()) {
            ci.cancel();
        }
    }


    @Inject(
            method = "handleAddObjective",
            at = @At("TAIL")
    )
    private void onObjectiveUpdate(
            ClientboundSetObjectivePacket packet,
            CallbackInfo ci
    ) {
        SkyblockData.updateObjective();
    }


    @Inject(
            method = "handleSetPlayerTeamPacket",
            at = @At("TAIL")
    )
    private void onScoreUpdate(
            ClientboundSetPlayerTeamPacket packet,
            CallbackInfo ci
    ) {
        SkyblockData.markScoreboardDirty();
    }


    @Inject(
            method = "handlePlayerInfoUpdate",
            at = @At("TAIL")
    )
    private void onTabListUpdate(
            ClientboundPlayerInfoUpdatePacket packet,
            CallbackInfo ci
    ) {
        SkyblockData.markTabListDirty();
    }


    @Inject(
            method = "handleLogin",
            at = @At("TAIL")
    )
    private void onJoinGame(
            ClientboundLoginPacket packet,
            CallbackInfo ci
    ) {
        new ServerJoinEvent().post();
    }
}