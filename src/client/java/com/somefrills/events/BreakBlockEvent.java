package com.somefrills.events;

import net.minecraft.util.math.BlockPos;

public class BreakBlockEvent extends Cancellable {
    public final BlockPos blockPos;

    public BreakBlockEvent(BlockPos blockPos) {
        this.blockPos = blockPos;
    }
}
