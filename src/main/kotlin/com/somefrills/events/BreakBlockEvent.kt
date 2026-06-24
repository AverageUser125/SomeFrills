package com.somefrills.events

import net.minecraft.core.BlockPos
import com.somefrills.events.FrillsEvent.Cancellable

class BreakBlockEvent(@JvmField val blockPos: BlockPos) : FrillsEvent(), Cancellable
