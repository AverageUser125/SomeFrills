package com.somefrills.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.somefrills.features.misc.Freecam;
import com.somefrills.features.tweaks.CameraTweaks;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    private boolean detached;

    @Inject(
            method = "getMaxZoom",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onClipToSpace(float cameraDist, CallbackInfoReturnable<Float> cir) {
        if (CameraTweaks.INSTANCE.clip()) {
            cir.setReturnValue(cameraDist);
        }
    }

    /*
    TODO: implement this
    @ModifyVariable(
            method = "getMaxZoom",
            at = @At("HEAD"),
            argsOnly = true,
            name = "cameraDist"
    )
    private float modifyGetMaxZoom(float cameraDist) {
        if (Freecam.INSTANCE.isActive()) {
            return 0;
        }

        if (CameraTweaks.INSTANCE.isActive()) {
            return (float) CameraTweaks.INSTANCE.getDistance();
        }

        return cameraDist;
    }
     */


    @Inject(
            method = "alignWithEntity",
            at = @At("TAIL")
    )
    private void onAlignTail(float partialTicks, CallbackInfo ci) {
        if (Freecam.INSTANCE.isActive()) {
            this.detached = true;
        }
    }


    @ModifyArgs(
            method = "alignWithEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;setPosition(DDD)V"
            )
    )
    private void onSetPosition(
            Args args,
            @Local(argsOnly = true, name = "partialTicks") float partialTicks
    ) {
        var freecam = Freecam.INSTANCE;

        if (freecam.isActive()) {
            args.set(0, freecam.getX(partialTicks));
            args.set(1, freecam.getY(partialTicks));
            args.set(2, freecam.getZ(partialTicks));
        }
    }


    @ModifyArgs(
            method = "alignWithEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;setRotation(FF)V"
            )
    )
    private void onSetRotation(
            Args args,
            @Local(argsOnly = true, name = "partialTicks") float partialTicks
    ) {
        var freecam = Freecam.INSTANCE;

        if (freecam.isActive()) {
            args.set(0, (float) freecam.getYaw(partialTicks));
            args.set(1, (float) freecam.getPitch(partialTicks));
        }
    }
}