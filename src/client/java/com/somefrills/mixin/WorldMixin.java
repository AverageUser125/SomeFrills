package com.somefrills.mixin;

import com.somefrills.events.BlockUpdateEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.somefrills.Main.eventBus;

@Mixin(World.class)
public abstract class WorldMixin {
    @Inject(method = "onBlockStateChanged", at = @At(value = "TAIL"))
    private void onBlockChanged(BlockPos pos, BlockState oldBlock, BlockState newBlock, CallbackInfo ci) {
        eventBus.post(new BlockUpdateEvent(pos, oldBlock, newBlock));
    }
}