package com.somefrills.features.mining;


import com.google.common.collect.Sets;
import com.somefrills.config.FrillsConfig;
import com.somefrills.events.BlockUpdateEvent;
import com.somefrills.features.core.AreaFeature;
import com.somefrills.misc.Area;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import java.util.HashSet;

import static com.somefrills.Main.mc;
import static net.minecraft.block.HorizontalConnectingBlock.*;

public class GemstoneDesyncFix extends AreaFeature {

    private static final HashSet<Area> islands = Sets.newHashSet(
            Area.DWARVEN_MINES,
            Area.CRYSTAL_HOLLOWS,
            Area.MINESHAFT,
            Area.CRIMSON_ISLE,
            Area.THE_RIFT
    );

    public GemstoneDesyncFix() {
        super(FrillsConfig.instance.mining.gemstoneDesyncFixEnabled);
    }

    public static boolean isDefaultPane(BlockState state) {
        return Utils.isStainedGlass(state) && !isConnectedPane(state);
    }

    public static boolean isConnectedPane(BlockState state) {
        return state.get(NORTH) || state.get(EAST) || state.get(SOUTH) || state.get(WEST);
    }

    public static BlockState asFullPane(BlockState state) {
        return state.with(NORTH, true).with(EAST, true).with(SOUTH, true).with(WEST, true);
    }

    @EventHandler
    private void onBlock(BlockUpdateEvent event) {
        if (event.newState.isAir() && Utils.isStainedGlass(event.oldState)) {
            event.newState.updateNeighbors(mc.world, event.pos, Block.NOTIFY_ALL);
        }
    }

    @Override
    protected boolean checkArea(Area area) {
        return islands.contains(area);
    }
}