package com.somefrills.events

import net.minecraft.network.protocol.Packet;

class ReceivePacketEvent(@JvmField val packet: Packet<*>) : Cancellable()
