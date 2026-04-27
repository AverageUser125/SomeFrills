package com.somefrills.features.misc.glowblock;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.somefrills.Main.mc;

public class BlockScanner {

    private final Set<ChunkPos> scannedChunks = new HashSet<>();
    private final Set<BlockPos> glowingBlocks = new HashSet<>();

    private ChunkPos lastCenter = null;

    public void reset() {
        clearResultsOnly();
        clearChunksOnly();
    }

    public void clearChunksOnly() {
        scannedChunks.clear();
        lastCenter = null;
    }

    public void clearResultsOnly() {
        glowingBlocks.clear();
    }

    public void removeBlockFromResults(Block block) {
        glowingBlocks.removeIf(pos ->
                mc.world.getBlockState(pos).getBlock() == block
        );
    }

    public Set<BlockPos> scanRenderedChunks(List<Block> targetBlocks) {
        if (mc.world == null || mc.player == null) {
            return glowingBlocks;
        }

        ChunkPos center = new ChunkPos(mc.player.getBlockPos());
        int radius = mc.options.getViewDistance().getValue();

        // optional: reset scan memory when player moves chunk
        if (lastCenter == null || !lastCenter.equals(center)) {
            lastCenter = center;
        }

        // scan ONLY 1 chunk per frame
        ChunkPos toScan = null;

        outer:
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {

                ChunkPos cp = new ChunkPos(center.x + dx, center.z + dz);

                if (scannedChunks.contains(cp)) continue;
                if (!mc.world.isChunkLoaded(cp.x, cp.z)) continue;

                toScan = cp;
                break outer;
            }
        }

        if (toScan == null) return glowingBlocks;

        scannedChunks.add(toScan);

        WorldChunk chunk = mc.world.getChunk(toScan.x, toScan.z);
        BlockPos basePos = chunk.getPos().getStartPos();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {

                int worldX = basePos.getX() + x;
                int worldZ = basePos.getZ() + z;

                for (int y = chunk.getBottomY(); y <= chunk.getTopYInclusive(); y++) {

                    BlockPos pos = new BlockPos(worldX, y, worldZ);

                    if (targetBlocks.contains(mc.world.getBlockState(pos).getBlock())) {
                        glowingBlocks.add(pos);
                    }
                }
            }
        }

        return glowingBlocks;
    }
}