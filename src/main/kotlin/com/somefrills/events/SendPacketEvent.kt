package com.somefrills.events

import net.minecraft.network.packet.Packet

class SendPacketEvent(val packet: Packet<*>) : Cancellable()
