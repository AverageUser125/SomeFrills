package com.somefrills.events

import net.minecraft.core.BlockPos

class BreakBlockEvent(@JvmField val blockPos: BlockPos) : Cancellable()
