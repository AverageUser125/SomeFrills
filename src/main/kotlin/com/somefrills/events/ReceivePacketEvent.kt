package com.somefrills.events

import net.minecraft.network.protocol.Packet;
import com.somefrills.events.FrillsEvent.Cancellable
import com.somefrills.modules.PrimaryFunction

class ReceivePacketEvent(@JvmField val packet: Packet<*>) : FrillsEvent(), Cancellable
