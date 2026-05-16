package com.somefrills.events

import net.minecraft.network.packet.s2c.play.ParticleS2CPacket
import net.minecraft.particle.ParticleType
import net.minecraft.util.math.Vec3d

class SpawnParticleEvent(var packet: ParticleS2CPacket) : Cancellable() {
    var type: ParticleType<*> = packet.parameters.type
    var pos: Vec3d? = Vec3d(packet.x, packet.y, packet.z)

    fun matchParameters(
        type: ParticleType<*>?,
        count: Int,
        speed: Double,
        offsetX: Double,
        offsetY: Double,
        offsetZ: Double
    ): Boolean {
        return this.type == type && this.packet.count == count && this.packet.speed == speed.toFloat()
                && this.packet.offsetX == offsetX.toFloat() && this.packet.offsetY == offsetY.toFloat() && this.packet.offsetZ == offsetZ.toFloat()
    }
}