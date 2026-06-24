package com.somefrills.mixin;

import com.somefrills.events.AttackEntityEvent;
import com.somefrills.events.BreakBlockEvent;
import com.somefrills.events.StartBreakingBlockEvent;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
    @Inject(method = "attack", at = @At("TAIL"), cancellable = true)
    private void onAttackEntity(Player player, Entity target, CallbackInfo ci) {
        if ((new AttackEntityEvent(target)).post().isCancelled()) ci.cancel();
    }

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void onBreakBlock(BlockPos blockPos, CallbackInfoReturnable<Boolean> info) {
        if (new BreakBlockEvent(blockPos).post().isCancelled()) info.setReturnValue(false);
    }

    @Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void onAttackBlock(BlockPos blockPos, Direction direction, CallbackInfoReturnable<Boolean> info) {
        if (new StartBreakingBlockEvent(blockPos, direction).post().isCancelled()) info.cancel();
    }
}
