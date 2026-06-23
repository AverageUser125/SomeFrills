package com.somefrills.events

import net.minecraft.world.phys.Vec3

class PlaySoundEvent(val soundName: String,
                     val location: Vec3,
                     val pitch: Float,
                     val volume: Float) : Cancellable()
