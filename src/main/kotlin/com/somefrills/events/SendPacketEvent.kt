package com.somefrills.events

import net.minecraft.network.protocol.Packet
import com.somefrills.events.FrillsEvent.Cancellable

class SendPacketEvent(val packet: Packet<*>) : FrillsEvent(), Cancellable
