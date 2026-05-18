package com.somefrills.utils

import com.somefrills.Main.mc
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier
import net.minecraft.client.sound.PositionedSoundInstance

object SoundUtils {
    fun playSoundInternal(event: SoundEvent, volume: Float, pitch: Float) {
        mc.soundManager.play(PositionedSoundInstance.master(event, pitch, volume))
    }

    fun playSoundInternal(event: RegistryEntry.Reference<SoundEvent>, volume: Float, pitch: Float) {
        playSoundInternal(event.value(), volume, pitch)
    }

    fun playSoundInternal(event: String, volume: Float, pitch: Float) {
        playSoundInternal(SoundEvent.of(Identifier.of(event)), volume, pitch)
    }
}

// ========== Sound Extension Functions ==========

fun playSound(event: SoundEvent, volume: Float = 1.0f, pitch: Float = 1.0f) {
    SoundUtils.playSoundInternal(event, volume, pitch)
}

fun playSound(event: RegistryEntry.Reference<SoundEvent>, volume: Float = 1.0f, pitch: Float = 1.0f) {
    SoundUtils.playSoundInternal(event, volume, pitch)
}

fun playSound(event: String, volume: Float = 1.0f, pitch: Float = 1.0f) {
    SoundUtils.playSoundInternal(event, volume, pitch)
}

val SoundEvent.play: Unit
    get() = playSound(this)

fun SoundEvent.play(volume: Float, pitch: Float = 1.0f) {
    playSound(this, volume, pitch)
}

fun RegistryEntry.Reference<SoundEvent>.play(volume: Float = 1.0f, pitch: Float = 1.0f) {
    playSound(this, volume, pitch)
}

