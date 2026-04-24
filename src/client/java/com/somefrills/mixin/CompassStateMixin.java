package com.somefrills.mixin;


import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.somefrills.features.core.Features;
import com.somefrills.features.misc.Freecam;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.item.property.numeric.CompassState;
import net.minecraft.util.HeldItemContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static com.somefrills.Main.mc;

@Mixin(CompassState.class)
public abstract class CompassStateMixin {
    @ModifyExpressionValue(method = "getBodyYaw", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/HeldItemContext;getBodyYaw()F"))
    private static float callLivingEntityGetYaw(float original) {
        if (Features.isActive(Freecam.class)) return mc.gameRenderer.getCamera().getYaw();
        return original;
    }

    @ModifyReturnValue(method = "getAngleTo(Lnet/minecraft/util/HeldItemContext;Lnet/minecraft/util/math/BlockPos;)D", at = @At("RETURN"))
    private static double modifyGetAngleTo(double original, HeldItemContext from, BlockPos to) {
        if (Features.isActive(Freecam.class)) {
            Vec3d vec3d = Vec3d.ofCenter(to);
            Camera camera = mc.gameRenderer.getCamera();
            return Math.atan2(vec3d.getZ() - camera.getCameraPos().z, vec3d.getX() - camera.getCameraPos().x) / (float) (Math.PI * 2);
        }

        return original;
    }
}
