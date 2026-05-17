package com.somefrills.events

import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.sound.SoundEvent

class PlaySoundEvent(val packet: PlaySoundS2CPacket) : Cancellable() {

    /**
     * Returns true if the SoundEvent from the packet matches the provided SoundEvent.
     */
    fun isSound(sound: SoundEvent): Boolean {
        return packet.sound.value().id() == sound.id()
    }

    fun isSound(sound: RegistryEntry.Reference<SoundEvent>): Boolean {
        return this.isSound(sound.value()!!)
    }
}
