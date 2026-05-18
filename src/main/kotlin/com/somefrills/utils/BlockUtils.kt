package com.somefrills.utils

import com.somefrills.Main.mc
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.StainedGlassBlock
import net.minecraft.block.StainedGlassPaneBlock
import net.minecraft.util.math.BlockPos

// ========== BlockState Extension Functions ==========

fun BlockState.isStainedGlass(): Boolean = block.isStainedGlass()

// ========== Block Extension Functions ==========

fun Block.isStainedGlass(): Boolean {
    return this is StainedGlassBlock || this is StainedGlassPaneBlock
}


// ========== BlockPos Extension Functions ==========

fun BlockPos.findGround(maxDistance: Int = 256): BlockPos {
    val dist = maxDistance.coerceIn(0, 256)
    val world = mc.world ?: return this
    for (i in 0..dist) {
        val below = down(i)
        if (!world.getBlockState(below).isAir) {
            return below
        }
    }
    return this
}
