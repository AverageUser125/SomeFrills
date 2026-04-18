package com.somefrills.mixin;

import com.somefrills.events.AttackEntityEvent;
import com.somefrills.events.BreakBlockEvent;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.somefrills.Main.eventBus;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {

    @Inject(method = "attackEntity", at = @At("TAIL"))
    private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        eventBus.post(new AttackEntityEvent(target));
    }

    @Inject(method = "breakBlock", at = @At("HEAD"), cancellable = true)
    private void onBreakBlock(BlockPos blockPos, CallbackInfoReturnable<Boolean> info) {
        if (eventBus.post(new BreakBlockEvent(blockPos)).isCancelled()) info.setReturnValue(false);
    }
}
