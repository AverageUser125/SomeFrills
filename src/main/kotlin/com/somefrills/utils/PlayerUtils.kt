package com.somefrills.utils

import com.somefrills.Main.mc
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.network.PlayerListEntry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Formatting
import net.minecraft.util.math.Box

object PlayerUtils {
    fun isRealPlayer(entity: PlayerEntity): Boolean {
        val handler: ClientPlayNetworkHandler = mc.networkHandler ?: return entity == mc.player

        val uuid = entity.uuid ?: return false
        if (uuid.version() != 4) return false

        val listEntry: PlayerListEntry = handler.getPlayerListEntry(uuid) ?: return entity == mc.player

        val displayName: String = listEntry.profile.name ?: return false
        val name = Formatting.strip(displayName) ?: return false
        return name.let { it.isNotEmpty() && !it.contains(" ") }
    }

    fun refillItemInternal(refillQuery: String, amount: Int) {
        var total = 0
        val player = mc.player ?: return
        val inv = player.inventory
        val query = refillQuery.replace("_", " ")
        for (i in 0 until 36) {
            val stack = inv.getStack(i)
            if (stack.isEmpty) continue
            val id = stack.skyblockId?.replace("_", " ") ?: continue
            val name = stack.name.toPlain()
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
            player.networkHandler.sendChatCommand(message.substring(1))
        } else {
            player.networkHandler.sendChatMessage(message)
        }
    }

    fun runCommand(command: String) {
        mc.player?.networkHandler?.sendChatCommand(command)
    }

    fun getHeldItem(): ItemStack = mc.player?.getHeldItem() ?: ItemStack.EMPTY

    fun isHoldingFishingRod(): Boolean = mc.player?.isHoldingFishingRod() == true
}

// ========== PlayerEntity Extension Functions ==========

fun PlayerEntity.isRealPlayer(): Boolean = PlayerUtils.isRealPlayer(this)

val PlayerEntity.playerName: String
    get() = Formatting.strip(gameProfile.name) ?: ""

fun PlayerEntity.getHeldItem(): ItemStack = mainHandStack

fun PlayerEntity.isHoldingFishingRod(): Boolean = getHeldItem().skyblockId?.contains("ROD") == true

fun PlayerEntity.refillItem(refillQuery: String, amount: Int) = PlayerUtils.refillItemInternal(refillQuery, amount)

fun PlayerEntity.isInZone(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double): Boolean {
    val area = Box(x1, y1, z1, x2, y2, z2)
    return area.contains(entityPos)
}

val PlayerEntity.cordsFormatted: String
    get() {
        val pos = blockPos
        return TextUtils.format("{},{},{}", pos.x, pos.y, pos.z)
    }

fun PlayerEntity.cordsFormatted(format: String): String {
    val pos = blockPos
    return TextUtils.format(format, pos.x, pos.y, pos.z)
}

fun PlayerEntity.isSelf(): Boolean = this === mc.player

// ========== Static Player State Extensions ==========

fun getHeldItem(): ItemStack = mc.player?.mainHandStack ?: ItemStack.EMPTY

fun isInZone(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double): Boolean {
    return mc.player?.isInZone(x1, y1, z1, x2, y2, z2) ?: false
}

val playerCordsFormatted: String
    get() = mc.player?.cordsFormatted ?: ""

fun playerCordsFormatted(format: String): String {
    return mc.player?.cordsFormatted(format) ?: ""
}
