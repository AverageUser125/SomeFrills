package com.somefrills.utils

import com.somefrills.Main.mc
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.StainedGlassBlock
import net.minecraft.world.level.block.StainedGlassPaneBlock
import net.minecraft.world.level.block.state.BlockState

// ========== BlockState Extension Functions ==========

fun BlockState.isStainedGlass(): Boolean = block.isStainedGlass()

// ========== Block Extension Functions ==========

fun Block.isStainedGlass(): Boolean {
    return this is StainedGlassBlock || this is StainedGlassPaneBlock
}


// ========== BlockPos Extension Functions ==========

fun BlockPos.findGround(maxDistance: Int = 256): BlockPos {
    val dist = maxDistance.coerceIn(0, 256)
    val world = mc.level ?: return this
    for (i in 0..dist) {
        val below = below(i)
        if (!world.getBlockState(below).isAir) {
            return below
        }
    }
    return this
}
