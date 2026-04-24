package com.somefrills.mixin;

import com.somefrills.features.core.Features;
import com.somefrills.features.tweaks.CameraTweaks;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class CameraMixin {
    @Inject(method = "clipToSpace", at = @At("HEAD"), cancellable = true)
    private void onClipToSpace(float desiredCameraDistance, CallbackInfoReturnable<Float> info) {
        if (Features.get(CameraTweaks.class).clip()) {
            info.setReturnValue(desiredCameraDistance);
        }
    }
}
