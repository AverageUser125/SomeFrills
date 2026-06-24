package com.somefrills.events

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import com.somefrills.events.FrillsEvent.Cancellable

class StartBreakingBlockEvent(val blockPos: BlockPos, val direction: Direction) : FrillsEvent(), Cancellable
