package com.somefrills.events

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class StartBreakingBlockEvent(var blockPos: BlockPos, var direction: Direction) : Cancellable()
