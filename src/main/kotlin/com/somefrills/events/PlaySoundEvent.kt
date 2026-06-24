package com.somefrills.events

import net.minecraft.client.resources.sounds.SoundInstance
import com.somefrills.events.FrillsEvent.Cancellable

class PlaySoundEvent(val sound: SoundInstance) : FrillsEvent(), Cancellable
