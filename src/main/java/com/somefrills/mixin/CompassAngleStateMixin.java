package com.somefrills.mixin;


import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.somefrills.features.misc.Freecam;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.item.properties.numeric.CompassAngleState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static com.somefrills.Main.mc;

@Mixin(CompassAngleState.class)
public abstract class CompassAngleStateMixin {
    @ModifyExpressionValue(method = "getWrappedVisualRotationY", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ItemOwner;getVisualRotationYInDegrees()F"))
    private static float callLivingEntityGetYaw(float original) {
        if (Freecam.INSTANCE.isActive()) return mc.gameRenderer.getCamera().getYaw();
        return original;
    }

    @ModifyReturnValue(method = "getAngleFromEntityToPos(Lnet/minecraft/world/entity/ItemOwner;Lnet/minecraft/core/BlockPos;)D", at = @At("RETURN"))
    private static double modifyGetAngleTo(double original, ItemOwner from, BlockPos to) {
        if (!Freecam.INSTANCE.isActive()) {
            return original;
        }
        Vec3 vec3d = Vec3.ofCenter(to);
        Camera camera = mc.gameRenderer.getCamera();
        return Math.atan2(vec3d.getZ() - camera.getCameraPos().z, vec3d.getX() - camera.getCameraPos().x) / (float) (Math.PI * 2);
    }
}
