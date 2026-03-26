package com.somefrills.events;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.BlockHitResult;

public class AttackBlockEvent {
    public BlockHitResult blockHitResult;
    public BlockPos blockPos;

    public AttackBlockEvent(BlockHitResult blockHitResult, BlockPos blockPos) {
        this.blockHitResult = blockHitResult;
        this.blockPos = blockPos;
    }
}
