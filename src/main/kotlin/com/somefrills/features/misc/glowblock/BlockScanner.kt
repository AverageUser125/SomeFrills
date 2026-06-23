package com.somefrills.features.misc.glowblock

import com.somefrills.Main.mc
import net.minecraft.core.BlockPos
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.Block

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
        val world = mc.level ?: return
        glowingBlocks.removeIf { pos: BlockPos -> world.getBlockState(pos).block === block }
    }

    fun scanRenderedChunks(targetBlocks: MutableList<Block>): MutableSet<BlockPos> {
        val player = mc.player ?: return glowingBlocks
        val world = mc.level ?: return glowingBlocks

        val center = ChunkPos(player.blockX, player.blockZ)
        val radius: Int = mc.options.renderDistance().get()

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
                if (!world.isLoaded(BlockPos(cp.x, 100, cp.z))) continue

                toScan = cp
                break@outer
            }
        }

        if (toScan == null) return glowingBlocks

        scannedChunks.add(toScan)

        val chunk = world.getChunk(toScan.x, toScan.z)
        val basePosX = chunk.getPos().minBlockX
        val basePosZ = chunk.getPos().minBlockZ

        for (x in 0..15) {
            for (z in 0..15) {
                val worldX = basePosX + x
                val worldZ = basePosZ + z

                for (y in chunk.minY..chunk.maxY) {
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