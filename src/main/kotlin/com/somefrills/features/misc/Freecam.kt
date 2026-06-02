package com.somefrills.features.misc

import com.somefrills.Main.mc
import com.somefrills.config.FrillsMod

import com.somefrills.events.*
import com.somefrills.features.core.FrillsFeature
import com.somefrills.features.core.ToggleFeature
import com.somefrills.misc.KeyAction
import com.somefrills.utils.ChatUtils
import com.somefrills.utils.set
import meteordevelopment.orbit.EventHandler
import meteordevelopment.orbit.EventPriority
import net.minecraft.client.option.Perspective
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import org.joml.Vector3d
import org.lwjgl.glfw.GLFW
import java.awt.event.KeyEvent
import kotlin.math.sqrt

@FrillsFeature
object Freecam : ToggleFeature(FrillsMod.config.misc.freecam.enabled, FrillsMod.config.misc.freecam.keybind) {
    private val config get() = FrillsMod.config.misc.freecam

    @JvmField
    val pos: Vector3d = Vector3d()

    @JvmField
    val prevPos: Vector3d = Vector3d()

    private var perspective: Perspective? = null

    @JvmField
    var yaw: Float = 0f

    @JvmField
    var pitch: Float = 0f

    @JvmField
    var lastYaw: Float = 0f

    @JvmField
    var lastPitch: Float = 0f

    private var fovScale = 1.0
    private var bobView = false

    private var forward = false
    private var backward = false
    private var right = false
    private var left = false
    private var up = false
    private var down = false

    public override fun onActivate() {
        super.onActivate()
        val player = mc.player ?: return
        if (mc.options == null || mc.gameRenderer == null || mc.gameRenderer.camera == null) {
            return
        }

        fovScale = mc.options.fovEffectScale.getValue()
        bobView = mc.options.bobView.getValue()
        if (config.staticView) {
            mc.options.fovEffectScale.value = 0.0
            mc.options.bobView.value = false
        }
        yaw = player.yaw
        pitch = player.pitch

        perspective = mc.options.perspective

        // FIXME: why is this here?
        //config.speed = config.speed

        pos.set(mc.gameRenderer.camera.cameraPos)
        prevPos.set(mc.gameRenderer.camera.cameraPos)

        if (mc.options.perspective == Perspective.THIRD_PERSON_FRONT) {
            yaw += 180f
            pitch *= -1f
        }

        lastYaw = yaw
        lastPitch = pitch

        // isSneaking = mc.options.sneakKey.isPressed;
        forward = mc.options.forwardKey.isPressed
        backward = mc.options.backKey.isPressed
        right = mc.options.rightKey.isPressed
        left = mc.options.leftKey.isPressed
        up = mc.options.jumpKey.isPressed
        down = mc.options.sneakKey.isPressed

        unpress()
        if (config.reloadChunks) mc.worldRenderer.reload()
    }

    public override fun onDeactivate() {
        super.onDeactivate()
        if (perspective == null) return
        if (config.reloadChunks) {
            mc.execute { mc.worldRenderer.reload() }
        }

        mc.options.perspective = perspective

        if (config.staticView) {
            mc.options.fovEffectScale.value = fovScale
            mc.options.bobView.value = bobView
        }

        perspective = null
        //isSneaking = false;
    }

    private fun unpress() {
        mc.options.forwardKey.isPressed = false
        mc.options.backKey.isPressed = false
        mc.options.rightKey.isPressed = false
        mc.options.leftKey.isPressed = false
        mc.options.jumpKey.isPressed = false
        mc.options.sneakKey.isPressed = false
    }

    @EventHandler
    private fun onOpenScreen(event: ScreenOpenEvent?) {
        unpress()

        prevPos.set(pos)
        lastYaw = yaw
        lastPitch = pitch
    }

    @EventHandler
    private fun onTick(event: TickEventPost) {
        val cameraEntity = mc.cameraEntity ?: return
        if (cameraEntity.isInsideWall) cameraEntity.noClip = true
        if (perspective == null) return
        if (!perspective!!.isFirstPerson) mc.options.perspective = Perspective.FIRST_PERSON

        val forward = Vec3d.fromPolar(0f, yaw)
        val right = Vec3d.fromPolar(0f, yaw + 90)
        var velX = 0.0
        var velY = 0.0
        var velZ = 0.0

        var s = 0.5
        if (mc.options.sprintKey.isPressed) s = 1.0

        var a = false
        if (this.forward) {
            velX += forward.x * s * config.speed
            velZ += forward.z * s * config.speed
            a = true
        }
        if (this.backward) {
            velX -= forward.x * s * config.speed
            velZ -= forward.z * s * config.speed
            a = true
        }

        var b = false
        if (this.right) {
            velX += right.x * s * config.speed
            velZ += right.z * s * config.speed
            b = true
        }
        if (this.left) {
            velX -= right.x * s * config.speed
            velZ -= right.z * s * config.speed
            b = true
        }

        if (a && b) {
            val diagonal = 1 / sqrt(2.0)
            velX *= diagonal
            velZ *= diagonal
        }

        if (this.up) {
            velY += s * config.speed
        }
        if (this.down) {
            velY -= s * config.speed
        }

        prevPos.set(pos)
        pos.set(pos.x + velX, pos.y + velY, pos.z + velZ)
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onKey(event: KeyDownEvent) {
        if (event.keyCode == GLFW.GLFW_KEY_F3) return

        if (onInput(event.keyCode, KeyAction.Press)) event.cancel()
    }

    @EventHandler
    fun onKey(event: KeyUpEvent) {
        if (event.keyCode == GLFW.GLFW_KEY_F3) return

        if (onInput(event.keyCode, KeyAction.Release)) event.cancel()
    }

    @EventHandler(priority = EventPriority.HIGH)
    private fun onMouseClick(event: MouseClickEvent) {
        if (onInput(event.button(), event.action)) event.cancel()
    }

    private fun onInput(key: Int, action: KeyAction): Boolean {
        when (key) {
           mc.options.forwardKey.boundKey.code -> {
                forward = action != KeyAction.Release
                mc.options.forwardKey.isPressed = false
            }

            mc.options.backKey.boundKey.code -> {
                backward = action != KeyAction.Release
                mc.options.backKey.isPressed = false
            }

            mc.options.rightKey.boundKey.code -> {
                right = action != KeyAction.Release
                mc.options.rightKey.isPressed = false
            }

            mc.options.leftKey.boundKey.code -> {
                left = action != KeyAction.Release
                mc.options.leftKey.isPressed = false
            }

            mc.options.jumpKey.boundKey.code -> {
                up = action != KeyAction.Release
                mc.options.jumpKey.isPressed = false
            }

            mc.options.sneakKey.boundKey.code -> {
                down = action != KeyAction.Release
                mc.options.sneakKey.isPressed = false
            }

            else -> {
                return false
            }
        }

        return true
    }

    @EventHandler(priority = EventPriority.LOW)
    private fun onMouseScroll(event: MouseScrollEvent) {
        if (config.speedScrollSensitivity > 0 && mc.currentScreen == null) {
            config.speed += event.value * 0.25 * (config.speedScrollSensitivity * config.speed)
            if (config.speed < 0.1) config.speed = 0.1

            event.cancel()
        }
    }


    @EventHandler
    private fun onChunkOcclusion(event: ChunkOcclusionEvent) {
        event.cancel()
    }

    @EventHandler
    private fun onGameLeft(event: GameStopEvent) {
        toggle()
    }

    @EventHandler
    private fun onPacketReceive(event: ReceivePacketEvent) {
        val packet = event.packet
        if (packet is DeathMessageS2CPacket) {
            val entity = mc.world?.getEntityById(packet.playerId()) ?: return
            if (entity === mc.player && config.toggleOnDeath) {
                toggle()
                ChatUtils.info("Toggled off because you died.")
            }
        } else if (packet is HealthUpdateS2CPacket) {
            val player = mc.player ?: return
            if (player.health - packet.health > 0 && config.toggleOnDamage) {
                toggle()
                ChatUtils.info("Toggled off because you took damage.")
            }
        } else if (packet is PlayerRespawnS2CPacket) {
            toggle()
            ChatUtils.info("Toggled off because you changed dimensions.")
        }
    }

    @EventHandler
    fun onInteractBlock(event: InteractBlockEvent) {
        event.cancel()
    }

    @EventHandler
    fun onInteractEntity(event: InteractEntityEvent) {
        event.cancel()
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onAttackEntity(event: AttackEntityEvent) {
        event.cancel()
    }

    @EventHandler(priority = EventPriority.HIGH)
    private fun onStartBreakingBlockEvent(event: StartBreakingBlockEvent) {
        event.cancel()
    }

    fun getX(tickDelta: Float): Double {
        return MathHelper.lerp(tickDelta.toDouble(), prevPos.x, pos.x)
    }

    fun getY(tickDelta: Float): Double {
        return MathHelper.lerp(tickDelta.toDouble(), prevPos.y, pos.y)
    }

    fun getZ(tickDelta: Float): Double {
        return MathHelper.lerp(tickDelta.toDouble(), prevPos.z, pos.z)
    }

    fun getYaw(tickDelta: Float): Double {
        return MathHelper.lerp(tickDelta, lastYaw, yaw).toDouble()
    }

    fun getPitch(tickDelta: Float): Double {
        return MathHelper.lerp(tickDelta, lastPitch, pitch).toDouble()
    }

    @JvmStatic
    fun shouldRenderHands(): Boolean {
        return config.renderHands
    }

    fun changeLookDirection(deltaX: Double, deltaY: Double) {
        lastYaw = yaw
        lastPitch = pitch

        yaw += deltaX.toFloat()
        pitch += deltaY.toFloat()

        pitch = MathHelper.clamp(pitch, -90f, 90f)
    }
}