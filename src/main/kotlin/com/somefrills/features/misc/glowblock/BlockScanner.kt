package com.somefrills.features.misc.glowblock

import com.somefrills.Main.mc
import net.minecraft.block.Block
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.chunk.WorldChunk

class BlockScanner {
    private val scannedChunks: MutableSet<ChunkPos> = HashSet<ChunkPos>()
    private val glowingBlocks: MutableSet<BlockPos> = HashSet<BlockPos>()

    private var lastCenter: ChunkPos? = null

    fun reset() {
        clearResultsOnly()
        clearChunksOnly()
    }

    fun clearChunksOnly() {
        scannedChunks.clear()
        lastCenter = null
    }

    fun clearResultsOnly() {
        glowingBlocks.clear()
    }

    fun removeBlockFromResults(block: Block?) {
        val world = mc.world ?: return
        glowingBlocks.removeIf { pos: BlockPos? -> world.getBlockState(pos).block === block }
    }

    fun scanRenderedChunks(targetBlocks: MutableList<Block>): MutableSet<BlockPos> {
        val player = mc.player ?: return glowingBlocks
        val world = mc.world ?: return glowingBlocks

        val center = ChunkPos(player.blockPos)
        val radius: Int = mc.options.viewDistance.getValue()

        // optional: reset scan memory when player moves chunk
        if (lastCenter == null || lastCenter != center) {
            lastCenter = center
        }

        // scan ONLY 1 chunk per frame
        var toScan: ChunkPos? = null

        outer@ for (dx in -radius..radius) {
            for (dz in -radius..radius) {
                val cp = ChunkPos(center.x + dx, center.z + dz)

                if (scannedChunks.contains(cp)) continue
                if (!world.isChunkLoaded(cp.x, cp.z)) continue

                toScan = cp
                break@outer
            }
        }

        if (toScan == null) return glowingBlocks

        scannedChunks.add(toScan)

        val chunk: WorldChunk = world.getChunk(toScan.x, toScan.z)
        val basePos = chunk.getPos().startPos

        for (x in 0..15) {
            for (z in 0..15) {
                val worldX = basePos.x + x
                val worldZ = basePos.z + z

                for (y in chunk.bottomY..chunk.topYInclusive) {
                    val pos = BlockPos(worldX, y, worldZ)

                    if (targetBlocks.contains(world.getBlockState(pos).block)) {
                        glowingBlocks.add(pos)
                    }
                }
            }
        }

        return glowingBlocks
    }
}