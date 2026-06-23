package com.somefrills.features.misc

import com.somefrills.config.FrillsMod

import com.somefrills.events.EntityUpdatedEvent
import com.somefrills.events.ServerJoinEvent
import com.somefrills.features.core.Feature
import com.somefrills.features.core.FrillsFeature
import com.somefrills.misc.RenderColor
import com.somefrills.utils.EntityUtils
import com.somefrills.utils.playerName
import com.somefrills.utils.setGlowing
import meteordevelopment.orbit.EventHandler
import meteordevelopment.orbit.EventPriority
import net.minecraft.world.entity.player.Player
import java.util.concurrent.ConcurrentHashMap

@FrillsFeature
object GlowPlayer : Feature(FrillsMod.config.misc.glowPlayer.enabled) {
    private val forcedGlows = ConcurrentHashMap<String, RenderColor>()

    private fun applyHighlight(entity: Player) {
        val pureName = entity.playerName
        val color = getColor(pureName)
        if (color != null) {
            entity.setGlowing(true, color)
        }
    }

    fun addPlayer(pureName: String, color: RenderColor): Boolean {
        return forcedGlows.put(pureName, color) == null
    }

    fun removePlayer(pureName: String): Boolean {
        if (forcedGlows.remove(pureName) == null) {
            return false
        }
        for (entity in EntityUtils.getPlayers()) {
            val entityPureName = entity.playerName
            if (pureName != entityPureName) continue
            entity.setGlowing(false, RenderColor.white)
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
        for (entity in EntityUtils.getPlayers()) {
            val pureName = entity.playerName
            if (!forcedGlows.containsKey(pureName)) continue
            entity.setGlowing(false, RenderColor.white)
        }
        forcedGlows.clear()
    }

    val forcedNames: MutableSet<String>
        get() = forcedGlows.keys

    fun setGlowImmediately(player: Player, color: RenderColor) {
        player.setGlowing(true, color)
    }

    @EventHandler
    private fun onServerJoin(event: ServerJoinEvent) {
        for (entity in EntityUtils.getPlayers()) {
            applyHighlight(entity)
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onEntityUpdate(event: EntityUpdatedEvent) {
        val entity = event.entity
        if (entity !is Player) return
        applyHighlight(entity)
    }
}