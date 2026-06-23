package com.somefrills.utils

import com.somefrills.Main.mc
import net.minecraft.client.resources.sounds.DirectionalSoundInstance
import net.minecraft.core.RegistryAccess
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource

object SoundUtils {

    fun playSoundInternal(
        event: SoundEvent,
        xAngle: Float,
        yAngle: Float,
        volume: Float = 1f
    ) {
        val sound = DirectionalSoundInstance(
            event,
            SoundSource.MASTER,
            RandomSource.create(),
            mc.gameRenderer.mainCamera,
            xAngle,
            yAngle
        )

        sound.volume = volume
        mc.soundManager.play(sound)
    }

    fun playSoundInternal(id: String, volume: Float, pitch: Float, yaw: Float = 0f) {
        val sound = SoundEvent.createVariableRangeEvent(Identifier.parse(id))
        playSoundInternal(sound, volume, pitch, yaw)
    }
}


fun playSound(event: SoundEvent, volume: Float = 1.0f, pitch: Float = 1.0f) {
    SoundUtils.playSoundInternal(event, volume, pitch)
}

fun playSound(id: String, volume: Float = 1.0f, pitch: Float = 1.0f) {
    SoundUtils.playSoundInternal(id, volume, pitch)
}

val SoundEvent.play: Unit
    get() = playSound(this)

fun SoundEvent.play(volume: Float = 1.0f, pitch: Float = 1.0f) {
    playSound(this, volume, pitch)
}