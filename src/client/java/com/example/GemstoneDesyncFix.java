package com.example;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StainedGlassBlock;
import net.minecraft.block.StainedGlassPaneBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import static net.minecraft.block.HorizontalConnectingBlock.*;

public class GemstoneDesyncFix {

    public static boolean active() {
        return AllConfig.gemstoneDsyncFix;
    }

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
        if (!active()) return;
        if (newState.isAir() && isStainedGlass(oldState)) {

            MinecraftClient mc = MinecraftClient.getInstance();
            newState.updateNeighbors(mc.world, pos, Block.NOTIFY_ALL);
        }
    }
}
