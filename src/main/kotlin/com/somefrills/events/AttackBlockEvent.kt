package com.somefrills.events

import net.minecraft.core.BlockPos
import net.minecraft.world.phys.BlockHitResult

class AttackBlockEvent(val blockHitResult: BlockHitResult, val blockPos: BlockPos) : FrillsEvent()
