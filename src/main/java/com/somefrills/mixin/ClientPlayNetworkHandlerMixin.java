// TODO(Ravel): Failed to fully resolve file: null cannot be cast to non-null type com.intellij.psi.PsiClass
package com.somefrills.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.somefrills.events.*;
import com.somefrills.misc.SkyblockData;
import com.somefrills.utils.TextUtils;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

import static com.somefrills.Main.eventBus;
import static com.somefrills.Main.mc;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @SuppressWarnings("unchecked")
    @Inject(method = "onEntityTrackerUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/syncher/SynchedEntityData;assignValues(Ljava/util/List;)V", shift = At.Shift.AFTER))
    private void onPostTrackerUpdate(EntityTrackerUpdateS2CPacket packet, CallbackInfo ci, @Local Entity ent) {
        if (ent instanceof LivingEntity || ent instanceof ItemEntity) {
            if (ent instanceof ArmorStand) {
                for (SynchedEntityData.DataValue<?> entry : packet.trackedValues()) {
                    if (entry.handler().equals(EntityDataSerializers.OPTIONAL_TEXT_COMPONENT) && entry.value() != null) {
                        ((Optional<Component>) entry.value()).ifPresent(value -> eventBus.post(new EntityNamedEvent(ent, value)));
                        break;
                    }
                }
            }
            eventBus.post(new EntityUpdatedEvent(ent));
        }
    }

    @Inject(method = "onEntitySpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;postAddEntitySoundInstance(Lnet/minecraft/world/entity/Entity;)V"))
    private void onEntitySpawn(EntitySpawnS2CPacket packet, CallbackInfo ci, @Local Entity ent) {
        eventBus.post(new EntityUpdatedEvent(ent));
    }

    @Inject(method = "onScreenHandlerSlotUpdate", at = @At("TAIL"))
    private void onUpdateInventory(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo ci) {
        if (mc.screen instanceof ContainerScreen container) {
            eventBus.post(new SlotUpdateEvent(packet, container, container.getScreenHandler(), packet.getSlot()));
        } else if (mc.screen == null) {
            eventBus.post(new InventoryUpdateEvent(packet, packet.getStack(), packet.getSlot()));
        }
    }

    @Inject(method = "onParticle", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V", shift = At.Shift.AFTER), cancellable = true)
    private void onParticle(ParticleS2CPacket packet, CallbackInfo ci) {
        if (eventBus.post(new SpawnParticleEvent(packet)).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "onScoreboardObjectiveUpdate", at = @At("TAIL"))
    private void onObjectiveUpdate(ScoreboardObjectiveUpdateS2CPacket packet, CallbackInfo ci) {
        SkyblockData.updateObjective();
    }

    @Inject(method = "onTeam", at = @At("TAIL"))
    private void onScoreUpdate(TeamS2CPacket packet, CallbackInfo ci) {
        SkyblockData.markScoreboardDirty();
    }

    @Inject(method = "onPlayerList", at = @At("TAIL"))
    private void onTabListUpdate(PlayerListS2CPacket packet, CallbackInfo ci) {
        SkyblockData.markTabListDirty();
    }

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void onJoinGame(GameJoinS2CPacket packet, CallbackInfo ci) {
        eventBus.post(new ServerJoinEvent());
    }

    @Inject(method = "onGameMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/chat/ChatListener;handleSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"), cancellable = true)
    private void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        String msg = TextUtils.toPlain(packet.content());
        if (!packet.overlay()) {
            if (eventBus.post(new ChatMsgEvent(packet.content(), msg)).isCancelled()) {
                ci.cancel();
            }
            if (msg.startsWith("Party > ") && msg.contains(": ")) {
                int nameStart = msg.contains("]") & msg.indexOf("]") < msg.indexOf(":") ? msg.indexOf("]") : msg.indexOf(">");
                String[] clean = msg.replace(msg.substring(0, nameStart + 1), "").split(":", 2);
                String author = clean[0].trim(), content = clean[1].trim();
                if (eventBus.post(new PartyChatMsgEvent(content, author)).isCancelled() && !ci.isCancelled()) {
                    ci.cancel();
                }
            }
        } else if (eventBus.post(new OverlayMsgEvent(packet.content(), msg)).isCancelled()) {
            ci.cancel();
        }
    }
}