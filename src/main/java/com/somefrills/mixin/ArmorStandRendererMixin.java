package com.somefrills.mixin;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStandRenderer.class)
public abstract class ArmorStandRendererMixin {
    @Inject(
            method = "getRenderType(Lnet/minecraft/client/renderer/entity/state/ArmorStandRenderState;ZZZ)Lnet/minecraft/client/renderer/rendertype/RenderType;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void blockBodyRenderForInvisibleGlowingArmorStand(
            ArmorStandRenderState state,
            boolean isBodyVisible,
            boolean forceTransparent,
            boolean appearGlowing,
            CallbackInfoReturnable<RenderType> cir
    ) {
        // Block body render for invisible glowing armor stands
        // Features (armor, items, elytra, head) will still render
        if (state.isInvisible && appearGlowing) {
            cir.setReturnValue(null);
        }
    }
}
