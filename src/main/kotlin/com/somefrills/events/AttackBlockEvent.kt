package com.somefrills.events

import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos

class AttackBlockEvent(val blockHitResult: BlockHitResult, val blockPos: BlockPos) : FrillsEvent()
