package com.somefrills.mixin;


import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.somefrills.features.core.Features;
import com.somefrills.features.misc.Freecam;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.debug.ChunkBorderDebugRenderer;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChunkBorderDebugRenderer.class)
public abstract class ChunkBorderDebugRendererMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/ChunkSectionPos;from(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/ChunkSectionPos;"))
    private ChunkSectionPos render$getChunkPos(ChunkSectionPos original) {
        Freecam freecam = Features.get(Freecam.class);
        if (!freecam.isActive()) return original;

        float delta = client.getRenderTickCounter().getTickProgress(true);

        return ChunkSectionPos.from(
                ChunkSectionPos.getSectionCoord(MathHelper.floor(freecam.getX(delta))),
                ChunkSectionPos.getSectionCoord(MathHelper.floor(freecam.getY(delta))),
                ChunkSectionPos.getSectionCoord(MathHelper.floor(freecam.getZ(delta)))
        );
    }
}