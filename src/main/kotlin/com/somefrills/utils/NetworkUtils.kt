package com.somefrills.utils

import com.somefrills.Main.mc
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket
import net.minecraft.util.Util

object NetworkUtils {
    fun sendPingPacket() {
        val handler = mc.networkHandler ?: return
        handler.sendPacket(QueryPingC2SPacket(Util.getMeasuringTimeMs()))
    }

    val isPingPending: Boolean
        get() {
            val handler = mc.networkHandler
            return handler != null
        }
}