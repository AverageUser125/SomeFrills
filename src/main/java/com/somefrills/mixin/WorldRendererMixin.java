package com.somefrills.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.somefrills.events.WorldRenderEvent;
import com.somefrills.features.core.Features;
import com.somefrills.features.misc.Freecam;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.somefrills.Main.eventBus;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow
    private WorldRenderer.Gizmos gizmos;
    @Shadow
    @Final
    private WorldRenderState worldRenderState;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4fStack;popMatrix()Lorg/joml/Matrix4fStack;"))
    private void onRenderWorld(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f matrix4f, Matrix4f projectionMatrix, GpuBufferSlice fogBuffer, Vector4f fogColor, boolean renderSky, CallbackInfo ci) {
        eventBus.post(new WorldRenderEvent(tickCounter, camera, new MatrixStack(), this.gizmos, this.worldRenderState));
        WorldRenderEvent.immediate.draw();
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;updateCamera(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;Z)V"), index = 2)
    private boolean renderSetupTerrainModifyArg(boolean spectator) {
        return Features.isActive(Freecam.class) || spectator;
    }
}
