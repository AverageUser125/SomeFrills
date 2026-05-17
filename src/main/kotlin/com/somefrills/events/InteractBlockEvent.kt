package com.somefrills.events

import net.minecraft.util.hit.BlockHitResult

class InteractBlockEvent(val blockHitResult: BlockHitResult) : Cancellable()
