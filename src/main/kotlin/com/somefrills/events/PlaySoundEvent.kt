package com.somefrills.events

import net.minecraft.util.math.Vec3d

class PlaySoundEvent(val soundName: String,
                     val location: Vec3d,
                     val pitch: Float,
                     val volume: Float) : Cancellable()
