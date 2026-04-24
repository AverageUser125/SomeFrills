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
    private boolean freecamSet = false;

    @Inject(method = "updateCrosshairTarget", at = @At("HEAD"), cancellable = true)
    private void updateTargetedEntityInvoke(float tickDelta, CallbackInfo info) {
        var freecam = Features.get(Freecam.class);

        if ((freecam.isActive()) && client.getCameraEntity() != null && !freecamSet) {
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

            ((IVec3d) cameraE.getEntityPos()).somefrills$set(freecam.pos.x, freecam.pos.y - cameraE.getEyeHeight(cameraE.getPose()), freecam.pos.z);
            cameraE.lastX = freecam.prevPos.x;
            cameraE.lastY = freecam.prevPos.y - cameraE.getEyeHeight(cameraE.getPose());
            cameraE.lastZ = freecam.prevPos.z;
            cameraE.setYaw(freecam.yaw);
            cameraE.setPitch(freecam.pitch);
            cameraE.lastYaw = freecam.lastYaw;
            cameraE.lastPitch = freecam.lastPitch;


            freecamSet = true;
            updateCrosshairTarget(tickDelta);
            freecamSet = false;

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
        if (!Features.get(Freecam.class).shouldRenderHands()) {
            ci.cancel();
        }
    }
}
