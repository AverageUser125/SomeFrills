package com.example.mixin;

import com.example.utils.AllConfig;
import net.minecraft.client.render.entity.feature.CreeperChargeFeatureRenderer;
import net.minecraft.client.render.entity.state.CreeperEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreeperChargeFeatureRenderer.class)
public abstract class CreeperEntityMixin {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void shouldRender(CreeperEntityRenderState creeperEntityRenderState, CallbackInfoReturnable<Boolean> cir) {
        // Hide charged creeper effect if config enabled
        if (AllConfig.creeperNotCharged) {
            cir.setReturnValue(false);
        }
    }
}


