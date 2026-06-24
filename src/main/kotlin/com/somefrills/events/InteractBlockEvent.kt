package com.somefrills.events

import net.minecraft.world.phys.BlockHitResult
import com.somefrills.events.FrillsEvent.Cancellable

class InteractBlockEvent(val blockHitResult: BlockHitResult) : FrillsEvent(), Cancellable
