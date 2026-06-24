package com.somefrills.features.misc.glowblock

import com.somefrills.Main.mc
import com.somefrills.config.FrillsMod

import com.somefrills.events.ServerJoinEvent
import com.somefrills.events.WorldRenderEvent
import com.somefrills.features.core.Feature
import com.somefrills.modules.FrillsFeature
import com.somefrills.misc.RenderColor
import com.somefrills.events.core.EventHandle
import net.minecraft.world.level.block.Block
import net.minecraft.world.phys.AABB

@FrillsFeature
object GlowBlock : Feature(FrillsMod.config.misc.glowBlock.enabled) {
    private var blockScanner: BlockScanner? = null

    @JvmField
    val targetBlocks: MutableList<Block> = ArrayList<Block>()

    public override fun onActivate() {
        if (blockScanner == null) {
            this.blockScanner = BlockScanner()
        }
        blockScanner?.reset()
    }

    public override fun onDeactivate() {
        blockScanner?.clearResultsOnly()
    }

    @EventHandle
    fun onServerJoin(event: ServerJoinEvent) {
        enabled = false
    }

    fun addBlock(block: Block) {
        if (targetBlocks.contains(block)) return
        targetBlocks.add(block)

        if (blockScanner != null) {
            blockScanner!!.clearChunksOnly()
        }
    }

    fun removeBlock(block: Block) {
        targetBlocks.remove(block)

        if (blockScanner != null) {
            blockScanner!!.removeBlockFromResults(block)
        }
    }

    fun clear() {
        targetBlocks.clear()
        blockScanner?.clearResultsOnly()
    }

    @EventHandle
    fun onWorldRender(event: WorldRenderEvent) {
        if (mc.level == null) return
        if (targetBlocks.isEmpty()) return

        val glowingBlocks = blockScanner?.scanRenderedChunks(targetBlocks) ?: return
        for (pos in glowingBlocks) {
            val box = AABB(pos)
            event.drawOutline(box, true, RenderColor(255, 255, 0, 128))
        }
    }
}
