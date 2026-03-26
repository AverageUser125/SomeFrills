package com.somefrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.somefrills.features.mining.GemstoneDesyncFix;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PaneBlock.class)
public abstract class PaneBlockMixin {

    @ModifyReturnValue(method = "updateShape", at = @At("RETURN"))
    private BlockState onGetUpdateState(BlockState original) {
        if (GemstoneDesyncFix.active() && GemstoneDesyncFix.isDefaultPane(original)) {
            return GemstoneDesyncFix.asFullPane(original);
        }
        return original;
    }
}