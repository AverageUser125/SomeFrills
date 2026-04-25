package com.somefrills.features.misc.glowblock;

import com.somefrills.config.FrillsConfig;
import com.somefrills.events.ServerJoinEvent;
import com.somefrills.events.WorldRenderEvent;
import com.somefrills.features.core.Feature;
import com.somefrills.misc.RenderColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.somefrills.Main.mc;

public class GlowBlock extends Feature {
    private BlockScanner blockScanner;
    private final List<Block> targetBlocks = new ArrayList<>();
    public GlowBlock() {
        super(FrillsConfig.instance.misc.glowBlock.enabled);
    }

    @Override
    public void onEnable() {
        if(blockScanner == null) {
            this.blockScanner = new BlockScanner();
        }
        blockScanner.reset();
    }

    @Override
    public void onDisable() {
        if(blockScanner != null) {
            blockScanner.clearResultsOnly();
        }
    }

    @EventHandler
    public void onServerJoin(ServerJoinEvent event) {
        setEnabled(false);
    }

    public void addBlock(Block block) {
        if (targetBlocks.contains(block)) return;
        targetBlocks.add(block);

        if (blockScanner != null) {
            blockScanner.reset();
        }
    }

    public void removeBlock(Block block) {
        targetBlocks.remove(block);

        if (blockScanner != null) {
            blockScanner.removeBlockFromResults(block);
        }
    }

    public void clear() {
        targetBlocks.clear();

        if (blockScanner != null) {
            blockScanner.clearResultsOnly();
        }
    }

    public List<Block> getTargetBlocks() {
        return targetBlocks;
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (mc.world == null || event.camera == null) return;
        if (targetBlocks.isEmpty()) return;

        Set<BlockPos> glowingBlocks = blockScanner.scanRenderedChunks(targetBlocks);
        for (BlockPos pos : glowingBlocks) {
            Box box = new Box(pos);
            event.drawOutline(box, true, new RenderColor(255, 255, 0, 128));
        }
    }

}
