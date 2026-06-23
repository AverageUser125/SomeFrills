package com.somefrills.events

import net.minecraft.network.protocol.Packet

class SendPacketEvent(val packet: Packet<*>) : Cancellable()
