package com.somefrills.utils

import com.somefrills.Main.mc
import net.minecraft.ChatFormatting
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.AABB

object PlayerUtils {
    fun isRealPlayer(entity: Player): Boolean {
        val handler = mc.connection ?: return entity == mc.player

        val uuid = entity.uuid ?: return false
        if (uuid.version() != 4) return false

        val listEntry = handler.getPlayerInfo(uuid) ?: return entity == mc.player

        val displayName: String = listEntry.profile.name ?: return false
        val name = ChatFormatting.stripFormatting(displayName) ?: return false
        return name.let { it.isNotEmpty() && !it.contains(" ") }
    }

    fun refillItemInternal(refillQuery: String, amount: Int) {
        var total = 0
        val player = mc.player ?: return
        val inv = player.inventory
        val query = refillQuery.replace("_", " ")
        for (i in 0 until 36) {
            val stack = inv.getItem(i)
            if (stack.isEmpty) continue
            val id = stack.skyblockId?.replace("_", " ") ?: continue
            val name = stack.displayName.toPlain()
            if (query.equals(id, ignoreCase = true) || query.equals(name, ignoreCase = true)) {
                total += stack.count
            }
        }
        if (total < amount) {
            sendMessage(TextUtils.format("/gfs {} {}", refillQuery, amount - total))
        }
    }


    fun sendMessage(message: String) {
        val player = mc.player ?: return
        if (message.isEmpty()) return
        if (message.startsWith("/")) {
            player.connection.sendCommand(message.substring(1))
        } else {
            player.connection.sendChat(message)
        }
    }

    fun runCommand(command: String) {
        mc.player?.connection?.sendCommand(command)
    }

    fun getHeldItem(): ItemStack = mc.player?.getHeldItem() ?: ItemStack.EMPTY

    fun isHoldingFishingRod(): Boolean = mc.player?.isHoldingFishingRod() == true
}

// ========== Player Extension Functions ==========

fun Player.isRealPlayer(): Boolean = PlayerUtils.isRealPlayer(this)

val Player.playerName: String
    get() = ChatFormatting.stripFormatting(gameProfile.name) ?: ""

fun Player.getHeldItem(): ItemStack = mainHandItem

fun Player.isHoldingFishingRod(): Boolean = getHeldItem().skyblockId?.contains("ROD") == true

fun Player.refillItem(refillQuery: String, amount: Int) = PlayerUtils.refillItemInternal(refillQuery, amount)

fun Player.isInZone(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double): Boolean {
    val area = AABB(x1, y1, z1, x2, y2, z2)
    return area.contains(position())
}

val Player.cordsFormatted: String
    get() {
        val pos = blockPosition()
        return TextUtils.format("{},{},{}", pos.x, pos.y, pos.z)
    }

fun Player.cordsFormatted(format: String): String {
    val pos = blockPosition()
    return TextUtils.format(format, pos.x, pos.y, pos.z)
}

fun Player.isSelf(): Boolean = this === mc.player

// ========== Static Player State Extensions ==========

fun getHeldItem(): ItemStack = mc.player?.mainHandItem ?: ItemStack.EMPTY

fun isInZone(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double): Boolean {
    return mc.player?.isInZone(x1, y1, z1, x2, y2, z2) ?: false
}

val playerCordsFormatted: String
    get() = mc.player?.cordsFormatted ?: ""

fun playerCordsFormatted(format: String): String {
    return mc.player?.cordsFormatted(format) ?: ""
}
