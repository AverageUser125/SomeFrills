package com.somefrills.events

import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.block.Block
import com.somefrills.events.FrillsEvent.Cancellable

class PlaceBlockEvent(@JvmField val context: BlockPlaceContext, val block: Block) : FrillsEvent(), Cancellable
