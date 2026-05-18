package com.somefrills.features.mining

import com.somefrills.config.FrillsMod

import com.somefrills.events.EntityUpdatedEvent
import com.somefrills.events.WorldRenderEvent
import com.somefrills.features.core.AreaFeature
import com.somefrills.features.core.FrillsFeature
import com.somefrills.misc.Area
import com.somefrills.misc.EntityCache
import com.somefrills.misc.RenderColor.Companion.fromChroma
import com.somefrills.utils.EntityUtils
import com.somefrills.utils.getLerpedBox
import meteordevelopment.orbit.EventHandler
import net.minecraft.entity.mob.CreeperEntity

@FrillsFeature
object GhostVision : AreaFeature(FrillsMod.config.mining.ghostVision.enabled) {
    @JvmStatic
    val config get() = FrillsMod.config.mining.ghostVision
    private val cache = EntityCache()

    @EventHandler
    private fun onEntity(event: EntityUpdatedEvent) {
        val entity = event.entity
        if (entity !is CreeperEntity) return
        if (config.removeCharge) {
            entity.getDataTracker().set(CreeperEntity.CHARGED, false)
        }
        cache.add(entity)
    }

    @EventHandler
    private fun onRender(event: WorldRenderEvent) {
        for (ent in cache.get()) {
            if (!ent.isAlive) continue
            val box = ent.getLerpedBox(event)
            event.drawStyled(
                box, config.style, false,
                fromChroma(config.outline), fromChroma(config.fill)
            )
        }
    }

    override fun checkArea(area: Area): Boolean {
        return area == Area.DWARVEN_MINES
    }
}
