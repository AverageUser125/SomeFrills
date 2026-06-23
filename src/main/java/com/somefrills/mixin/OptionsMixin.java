package com.somefrills.mixin;


import com.somefrills.features.core.Features;
import com.somefrills.features.misc.Freecam;
import net.minecraft.client.Options;
import net.minecraft.client.CameraType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public abstract class OptionsMixin {
    @Inject(method = "setCameraType", at = @At("HEAD"), cancellable = true)
    private void setPerspective(CameraType cameraType, CallbackInfo info) {
        if (!Features.isInitialized()) return;
        if (Freecam.INSTANCE.isActive()) info.cancel();
    }
}
