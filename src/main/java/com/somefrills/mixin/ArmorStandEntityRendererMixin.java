package com.somefrills.mixin;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.ArmorStandEntityRenderer;
import net.minecraft.client.render.entity.state.ArmorStandEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStandEntityRenderer.class)
public abstract class ArmorStandEntityRendererMixin {
    @Inject(
            method = "getRenderLayer(Lnet/minecraft/client/render/entity/state/ArmorStandEntityRenderState;ZZZ)Lnet/minecraft/client/render/RenderLayer;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void blockBodyRenderForInvisibleGlowingArmorStand(
            ArmorStandEntityRenderState state,
            boolean showBody,
            boolean translucent,
            boolean showOutline,
            CallbackInfoReturnable<RenderLayer> cir
    ) {
        // Block body render for invisible glowing armor stands
        // Features (armor, items, elytra, head) will still render
        if (state.invisible && showOutline) {
            cir.setReturnValue(null);
        }
    }
}
