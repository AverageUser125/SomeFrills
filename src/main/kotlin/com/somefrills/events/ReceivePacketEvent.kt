package com.somefrills.events

import net.minecraft.network.packet.Packet

class ReceivePacketEvent(@JvmField var packet: Packet<*>) : Cancellable()
