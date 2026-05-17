package com.somefrills.events

import net.minecraft.network.packet.Packet

class ReceivePacketEvent(@JvmField val packet: Packet<*>) : Cancellable()
