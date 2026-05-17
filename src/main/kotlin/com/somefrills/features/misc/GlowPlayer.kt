package com.somefrills.features.misc

import com.somefrills.config.FrillsMod

import com.somefrills.events.EntityUpdatedEvent
import com.somefrills.events.ServerJoinEvent
import com.somefrills.features.core.Feature
import com.somefrills.features.core.FrillsFeature
import com.somefrills.misc.RenderColor
import com.somefrills.misc.Utils
import meteordevelopment.orbit.EventHandler
import meteordevelopment.orbit.EventPriority
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import java.util.concurrent.ConcurrentHashMap

@FrillsFeature
class GlowPlayer : Feature(FrillsMod.config.misc.glowPlayer.enabled) {
    private val forcedGlows = ConcurrentHashMap<String, RenderColor>()

    private fun applyHighlight(entity: Entity) {
        if (entity !is PlayerEntity) return
        val pureName = Utils.getPlayerName(entity)
        val color = getColor(pureName)
        if (color != null) {
            Utils.setGlowing(entity, true, color)
        }
    }

    fun addPlayer(pureName: String, color: RenderColor): Boolean {
        return forcedGlows.put(pureName, color) == null
    }

    fun removePlayer(pureName: String): Boolean {
        if (forcedGlows.remove(pureName) == null) {
            return false
        }
        for (entity in Utils.getEntities()) {
            if (entity !is PlayerEntity) continue
            val entityPureName = Utils.getPlayerName(entity)
            if (pureName != entityPureName) continue
            Utils.setGlowing(entity, false, RenderColor.white)
            break
        }
        return true
    }

    fun hasPlayer(pureName: String): Boolean {
        return forcedGlows.containsKey(pureName)
    }

    fun getColor(pureName: String): RenderColor? {
        return forcedGlows[pureName]
    }

    fun clear() {
        for (entity in Utils.getEntities()) {
            if (entity !is PlayerEntity) continue
            val pureName = Utils.getPlayerName(entity)
            if (pureName == null || !forcedGlows.containsKey(pureName)) continue
            Utils.setGlowing(entity, false, RenderColor.white)
        }
        forcedGlows.clear()
    }

    val forcedNames: MutableSet<String>
        get() = forcedGlows.keys

    fun setGlowImmediately(player: AbstractClientPlayerEntity, color: RenderColor) {
        Utils.setGlowing(player, true, color)
    }

    @EventHandler
    private fun onServerJoin(event: ServerJoinEvent) {
        for (entity in Utils.getEntities()) {
            applyHighlight(entity)
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onEntityUpdate(event: EntityUpdatedEvent) {
        val entity = event.entity
        applyHighlight(entity)
    }
}