package com.somefrills.mixin;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.YggdrasilServicesKeyInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(YggdrasilServicesKeyInfo.class)
public class YggdrasilServicesKeyInfoMixin {
    @Inject(method = "validateProperty", at = @At("HEAD"), cancellable = true)
    private void onValidateProperty(Property property, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
