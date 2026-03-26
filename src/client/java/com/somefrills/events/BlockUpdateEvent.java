package com.somefrills.events;

import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;

public class BlockUpdateEvent {

    public BlockPos pos;
    public BlockState oldState;
    public BlockState newState;

    public BlockUpdateEvent(BlockPos pos, BlockState oldState, BlockState newState) {
        this.pos = pos;
        this.oldState = oldState;
        this.newState = newState;
    }
}
