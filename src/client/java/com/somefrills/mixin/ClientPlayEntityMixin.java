package com.somefrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.somefrills.features.core.Features;
import com.somefrills.features.mining.NoMiningTrace;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayEntityMixin {
    // https://github.com/MeteorDevelopment/meteor-client/blob/master/src/main/java/meteordevelopment/meteorclient/mixin/ClientPlayerEntityMixin.java#L123
    @ModifyReturnValue(method = "getCrosshairTarget(Lnet/minecraft/entity/Entity;DDF)Lnet/minecraft/util/hit/HitResult;", at = @At("RETURN"))
    private static HitResult onUpdateTargetedEntity(HitResult original, @Local HitResult hitResult) {
        if (original instanceof EntityHitResult ehr) {
            if (Features.get(NoMiningTrace.class).canWork(ehr.getEntity()) && hitResult.getType() == HitResult.Type.BLOCK) {
                return hitResult;
            }
        }

        return original;
    }

}
