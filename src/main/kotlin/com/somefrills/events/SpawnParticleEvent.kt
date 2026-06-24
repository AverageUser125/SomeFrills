package com.somefrills.events

import net.minecraft.core.particles.ParticleType
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket
import net.minecraft.world.phys.Vec3
import com.somefrills.events.FrillsEvent.Cancellable

class SpawnParticleEvent(packet: ClientboundLevelParticlesPacket) : FrillsEvent(), Cancellable {
    var packet: ClientboundLevelParticlesPacket
    var type: ParticleType<*>
    var pos: Vec3?

    init {
        this.packet = packet
        this.type = packet.particle.type
        this.pos = Vec3(packet.x, packet.y, packet.z)
    }

    fun matchParameters(
        type: ParticleType<*>?,
        count: Int,
        speed: Double,
        offsetX: Double,
        offsetY: Double,
        offsetZ: Double
    ): Boolean {
        return this.type == type && this.packet.getCount() == count && this.packet.getMaxSpeed() == speed.toFloat() && this.packet.getXDist() == offsetX.toFloat() && this.packet.getYDist() == offsetY.toFloat() && this.packet.getZDist() == offsetZ.toFloat()
    }

    val isCurveParticle: Boolean
        get() = this.matchParameters(ParticleTypes.ENCHANT, 10, -2.0, 0.0, 0.0, 0.0)

    val particleId: String
        get() {
            val identifier = BuiltInRegistries.PARTICLE_TYPE.getKey(this.type)
            return identifier?.toString() ?: ""
        }
}