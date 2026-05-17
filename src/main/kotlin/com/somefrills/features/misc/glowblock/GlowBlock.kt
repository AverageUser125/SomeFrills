package com.somefrills.features.misc.glowblock

import com.somefrills.Main.mc
import com.somefrills.config.FrillsConfig
import com.somefrills.events.ServerJoinEvent
import com.somefrills.events.WorldRenderEvent
import com.somefrills.features.core.Feature
import com.somefrills.features.core.FrillsFeature
import com.somefrills.misc.RenderColor
import meteordevelopment.orbit.EventHandler
import net.minecraft.block.Block
import net.minecraft.util.math.Box

@FrillsFeature
class GlowBlock : Feature(FrillsConfig.misc.glowBlock.enabled) {
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

    @EventHandler
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

    @EventHandler
    fun onWorldRender(event: WorldRenderEvent) {
        if (mc.world == null) return
        if (targetBlocks.isEmpty()) return

        val glowingBlocks = blockScanner?.scanRenderedChunks(targetBlocks) ?: return
        for (pos in glowingBlocks) {
            val box = Box(pos)
            event.drawOutline(box, true, RenderColor(255, 255, 0, 128))
        }
    }
}
