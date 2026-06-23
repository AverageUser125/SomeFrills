package com.somefrills.utils

import com.somefrills.Main.mc
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket
import net.minecraft.util.Util

object NetworkUtils {
    fun sendPingPacket() {
        val handler = mc.connection ?: return
        handler.send(ServerboundPingRequestPacket(System.currentTimeMillis()))
    }

    val isPingPending: Boolean
        get() {
            val handler = mc.connection
            return handler != null
        }
}