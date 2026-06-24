package com.somefrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.somefrills.Main;
import com.somefrills.events.*;
import com.somefrills.features.misc.Freecam;
import com.somefrills.mixininterface.IVec3;
import com.somefrills.utils.EntityUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.Profiler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Minecraft.class, priority = 999)
public abstract class MinecraftMixin {
    @Shadow
    @Nullable
    public ClientLevel level;

    @Shadow
    public abstract void setScreen(@Nullable Screen screen);

    @Shadow
    public abstract Entity getCameraEntity();

    @ModifyReturnValue(method = "shouldEntityAppearGlowing", at = @At("RETURN"))
    private boolean hasOutline(boolean original, Entity entity) {
        if (EntityUtils.isGlowing(entity)) {
            return true;
        }
        return original;
    }

    @Inject(method = "setScreen", at = @At("TAIL"))
    private void onOpenScreen(Screen screen, CallbackInfo ci) {
        if (this.level == null) return;
        if (screen != null) {
            new ScreenOpenEvent(screen).post();
        } else {
            new ScreenCloseEvent().post();
        }
    }

    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/EntityHitResult;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"), cancellable = true)
    private void onInteractEntity(CallbackInfo ci, @Local(name = "entity") Entity entity, @Local(name = "entityHit") EntityHitResult entityHit) {
        if ((new InteractEntityEvent(entity, entityHit)).post().isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;useItemOn(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"), cancellable = true)
    private void onInteractBlock(CallbackInfo ci, @Local(name = "blockHit") BlockHitResult blockHit) {
        if ((new InteractBlockEvent(blockHit)).post().isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;useItem(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"), cancellable = true)
    private void onInteractItem(CallbackInfo ci) {
        if ((new InteractItemEvent()).post().isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startDestroyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z"))
    private void onAttackBlock(CallbackInfoReturnable<Boolean> cir, @Local(name = "blockHit") BlockHitResult blockHit, @Local(name = "pos") BlockPos pos) {
        new AttackBlockEvent(blockHit, pos).post();
    }

    @Inject(method = "destroy", at = @At("HEAD"))
    private void beforeStop(CallbackInfo ci) {
        new GameStopEvent().post();
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void onPreTick(CallbackInfo info) {
        Profiler.get().push(Main.MOD_ID + "_pre_update");
        new TickEventPre().post();
        Profiler.get().pop();
    }

    @Inject(at = @At("TAIL"), method = "tick")
    private void onTick(CallbackInfo info) {
        Profiler.get().push(Main.MOD_ID + "_post_update");
        new TickEventPost().post();
        Profiler.get().pop();
    }

    @Shadow
    protected abstract void pick(float tickDelta);

    @Unique
    private boolean freeCamSet = false;

    @Inject(method = "pick", at = @At("HEAD"), cancellable = true)
    private void updateTargetedEntityInvoke(float tickDelta, CallbackInfo info) {
        if ((Freecam.INSTANCE.isActive()) && this.getCameraEntity() != null && !freeCamSet) {
            info.cancel();
            Entity cameraE = this.getCameraEntity();

            double x = cameraE.getX();
            double y = cameraE.getY();
            double z = cameraE.getZ();
            double lastX = cameraE.xo;
            double lastY = cameraE.yo;
            double lastZ = cameraE.zo;
            float yaw = cameraE.getYRot();
            float pitch = cameraE.getXRot();
            float lastYaw = cameraE.yRotO;
            float lastPitch = cameraE.xRotO;

            ((IVec3) cameraE.position()).somefrills$set(Freecam.pos.x, Freecam.pos.y - cameraE.getEyeHeight(cameraE.getPose()), Freecam.pos.z);
            cameraE.xo = Freecam.prevPos.x;
            cameraE.yo = Freecam.prevPos.y - cameraE.getEyeHeight(cameraE.getPose());
            cameraE.zo = Freecam.prevPos.z;
            cameraE.setYRot(Freecam.yaw);
            cameraE.setXRot(Freecam.pitch);
            cameraE.yRotO = Freecam.lastYaw;
            cameraE.xRotO = Freecam.lastPitch;


            freeCamSet = true;
            pick(tickDelta);
            freeCamSet = false;

            ((IVec3) cameraE.position()).somefrills$set(x, y, z);
            cameraE.xo = lastX;
            cameraE.yo = lastY;
            cameraE.zo = lastZ;
            cameraE.setYRot(yaw);
            cameraE.setXRot(pitch);
            cameraE.yRotO = lastYaw;
            cameraE.xRotO = lastPitch;
        }
    }

}
