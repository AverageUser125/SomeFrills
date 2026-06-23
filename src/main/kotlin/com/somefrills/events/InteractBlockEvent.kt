package com.somefrills.events

import net.minecraft.world.phys.BlockHitResult

class InteractBlockEvent(val blockHitResult: BlockHitResult) : Cancellable()
