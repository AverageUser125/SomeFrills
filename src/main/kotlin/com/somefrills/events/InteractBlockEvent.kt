package com.somefrills.events

import net.minecraft.util.hit.BlockHitResult

class InteractBlockEvent(var blockHitResult: BlockHitResult?) : Cancellable()
