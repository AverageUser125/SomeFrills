package com.somefrills.events

import net.minecraft.util.math.BlockPos

class BreakBlockEvent(@JvmField val blockPos: BlockPos) : Cancellable()
