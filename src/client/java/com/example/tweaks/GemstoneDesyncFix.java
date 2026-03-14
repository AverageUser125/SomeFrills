package com.example.tweaks;

import com.example.utils.AllConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StainedGlassBlock;
import net.minecraft.block.StainedGlassPaneBlock;
import net.minecraft.util.math.BlockPos;

import static com.example.Main.mc;
import static net.minecraft.block.HorizontalConnectingBlock.*;

// https://github.com/WhatYouThing/NoFrills/blob/08b2e81e228a4e74bc5afacf9c234ce7a2e569a8/src/main/java/nofrills/features/mining/GemstoneDesyncFix.java#L18
public class GemstoneDesyncFix {
    public static boolean isStainedGlass(BlockState state) {
        Block block = state.getBlock();
        return block instanceof StainedGlassBlock || block instanceof StainedGlassPaneBlock;
    }

    public static boolean isDefaultPane(BlockState state) {
        return isStainedGlass(state) && !isConnectedPane(state);
    }

    private static boolean isConnectedPane(BlockState state) {
        return state.get(NORTH) || state.get(EAST) || state.get(SOUTH) || state.get(WEST);
    }

    public static BlockState asFullPane(BlockState state) {
        return state.with(NORTH, true).with(EAST, true).with(SOUTH, true).with(WEST, true);
    }

    public static void onBlock(BlockPos pos, BlockState oldState, BlockState newState) {
        if (!AllConfig.gemstoneDsyncFix) return;
        if (newState.isAir() && isStainedGlass(oldState)) {
            newState.updateNeighbors(mc.world, pos, Block.NOTIFY_ALL);
        }
    }
}
