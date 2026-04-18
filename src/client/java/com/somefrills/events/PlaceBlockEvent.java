package com.somefrills.events;

import net.minecraft.block.Block;
import net.minecraft.item.ItemPlacementContext;

public class PlaceBlockEvent extends Cancellable {
    public final ItemPlacementContext context;
    public final Block block;

    public PlaceBlockEvent(ItemPlacementContext context, Block block) {
        this.context = context;
        this.block = block;
    }
}
