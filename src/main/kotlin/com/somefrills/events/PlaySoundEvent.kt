package com.somefrills.events

import net.minecraft.client.resources.sounds.SoundInstance

class PlaySoundEvent(val sound: SoundInstance) : Cancellable()
