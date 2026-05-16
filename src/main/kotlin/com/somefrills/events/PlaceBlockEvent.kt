package com.somefrills.events

import net.minecraft.block.Block
import net.minecraft.item.ItemPlacementContext

class PlaceBlockEvent(@JvmField val context: ItemPlacementContext, val block: Block) : Cancellable()
