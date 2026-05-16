package com.somefrills.events

import net.minecraft.network.packet.Packet

class SendPacketEvent(var packet: Packet<*>) : Cancellable()
