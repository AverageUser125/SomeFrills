package com.somefrills.features.mining.pingoffsetminer;

import com.somefrills.config.FrillsConfig;
import com.somefrills.misc.Area;
import com.somefrills.misc.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;

import static com.somefrills.Main.mc;

public class PomBlock {
    private BlockPos pos;
    private VoxelShape shape;
    private long hardness;
    private String name;
    private Block block;

    public PomBlock() {
        this.pos = null;
        this.shape = null;
        this.hardness = -1;
        this.name = "";
        this.block = null;
    }

    public BlockPos getBlockPos() {
        return this.pos;
    }

    public VoxelShape getShape() {
        return this.shape;
    }

    public long getHardness() {
        return this.hardness;
    }

    public String getName() {
        return this.name;
    }

    public void setBlock() {
        HitResult hr = mc.crosshairTarget;
        if (hr == null || mc.world == null) return;

        if (hr.getType() != HitResult.Type.BLOCK) {
            this.pos = null;
            this.shape = null;
            this.hardness = -1;
            this.name = "";
            this.block = null;
            return;
        }
        BlockPos blockPos = ((BlockHitResult) hr).getBlockPos();

        BlockState blockState = mc.world.getBlockState(blockPos);

        Block block = blockState.getBlock();
        if (block == this.block) return;
        String blockName = SpeedCalc.getBlockName(block);

        if (block == Blocks.COBBLESTONE && Utils.isInArea(Area.MINESHAFT)) {
            blockName = SpeedCalc.getBlockName(Blocks.INFESTED_COBBLESTONE);
        }

        if (FrillsConfig.instance.mining.pingOffsetMiner.blockEnabled.get().getOrDefault(blockName, false)) {
            this.shape = blockState.getCollisionShape(mc.world, blockPos);
            this.pos = blockPos;
            this.hardness = SpeedCalc.blockHardness.get(blockName);
            this.name = blockName;
            this.block = block;
        }
    }

    public boolean isEmpty() {
        return this.shape == null && this.pos == null;
    }
}
