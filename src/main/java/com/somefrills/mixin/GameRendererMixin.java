package com.somefrills.mixin;

import com.somefrills.features.core.Features;
import com.somefrills.features.misc.Freecam;
import com.somefrills.mixininterface.IVec3d;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    public abstract void updateCrosshairTarget(float tickDelta);

    @Unique
    private boolean freeCamSet = false;

    @Inject(method = "updateCrosshairTarget", at = @At("HEAD"), cancellable = true)
    private void updateTargetedEntityInvoke(float tickDelta, CallbackInfo info) {
        if ((Freecam.INSTANCE.isActive()) && client.getCameraEntity() != null && !freeCamSet) {
            info.cancel();
            Entity cameraE = client.getCameraEntity();

            double x = cameraE.getX();
            double y = cameraE.getY();
            double z = cameraE.getZ();
            double lastX = cameraE.lastX;
            double lastY = cameraE.lastY;
            double lastZ = cameraE.lastZ;
            float yaw = cameraE.getYaw();
            float pitch = cameraE.getPitch();
            float lastYaw = cameraE.lastYaw;
            float lastPitch = cameraE.lastPitch;

            ((IVec3d) cameraE.getEntityPos()).somefrills$set(Freecam.pos.x, Freecam.pos.y - cameraE.getEyeHeight(cameraE.getPose()), Freecam.pos.z);
            cameraE.lastX = Freecam.prevPos.x;
            cameraE.lastY = Freecam.prevPos.y - cameraE.getEyeHeight(cameraE.getPose());
            cameraE.lastZ = Freecam.prevPos.z;
            cameraE.setYaw(Freecam.yaw);
            cameraE.setPitch(Freecam.pitch);
            cameraE.lastYaw = Freecam.lastYaw;
            cameraE.lastPitch = Freecam.lastPitch;


            freeCamSet = true;
            updateCrosshairTarget(tickDelta);
            freeCamSet = false;

            ((IVec3d) cameraE.getEntityPos()).somefrills$set(x, y, z);
            cameraE.lastX = lastX;
            cameraE.lastY = lastY;
            cameraE.lastZ = lastZ;
            cameraE.setYaw(yaw);
            cameraE.setPitch(pitch);
            cameraE.lastYaw = lastYaw;
            cameraE.lastPitch = lastPitch;
        }
    }

    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    private void renderHand(float tickProgress, boolean sleeping, Matrix4f positionMatrix, CallbackInfo ci) {
        if (!Freecam.shouldRenderHands()) {
            ci.cancel();
        }
    }
}
