package com.somefrills.events

import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos

class AttackBlockEvent(var blockHitResult: BlockHitResult, var blockPos: BlockPos) : FrillsEvent()
