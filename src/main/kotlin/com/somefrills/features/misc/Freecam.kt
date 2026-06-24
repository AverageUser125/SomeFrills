package com.somefrills.features.misc

import com.somefrills.Main
import com.somefrills.Main.mc
import com.somefrills.config.FrillsMod
import com.somefrills.events.*
import com.somefrills.modules.FrillsFeature
import com.somefrills.features.core.ToggleFeature
import com.somefrills.misc.KeyAction
import com.somefrills.mixin.KeyMappingAccessor
import com.somefrills.utils.ChatUtils
import com.somefrills.utils.set
import com.somefrills.events.core.EventHandle
import com.somefrills.events.core.EventPriority
import net.minecraft.client.CameraType
import net.minecraft.client.KeyMapping
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket
import net.minecraft.network.protocol.game.ClientboundRespawnPacket
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import org.joml.Vector3d
import org.lwjgl.glfw.GLFW
import kotlin.math.sqrt


@FrillsFeature
object Freecam : ToggleFeature(FrillsMod.config.misc.freecam.enabled, FrillsMod.config.misc.freecam.keybind) {
    private val config get() = FrillsMod.config.misc.freecam

    @JvmField
    val pos: Vector3d = Vector3d()

    @JvmField
    val prevPos: Vector3d = Vector3d()

    private var perspective: CameraType? = null

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
        fovScale = mc.options.fovEffectScale.get()
        bobView = mc.options.bobView.get()
        if (config.staticView) {
            mc.options.fovEffectScale.set(0.0)
            mc.options.bobView.set(false)
        }

        yaw = player.getYRot();
        pitch = player.getXRot();

        perspective = mc.options.cameraType

        // FIXME: why is this here?
        //config.speed = config.speed

        pos.set(mc.gameRenderer.mainCamera.position())
        prevPos.set(mc.gameRenderer.mainCamera.position())

        if (mc.options.cameraType == CameraType.THIRD_PERSON_FRONT) {
            yaw += 180f
            pitch *= -1f
        }

        lastYaw = yaw
        lastPitch = pitch

        // isSneaking = mc.options.sneakKey.isPressed;
        forward = mc.options.keyUp.isDown
        backward = mc.options.keyDown.isDown
        right = mc.options.keyRight.isDown
        left = mc.options.keyLeft.isDown
        up = mc.options.keyJump.isDown
        down = mc.options.keyShift.isDown

        unpress()
        if (config.reloadChunks) mc.levelRenderer.allChanged()
    }

    public override fun onDeactivate() {
        super.onDeactivate()
        if (config.reloadChunks) {
            mc.execute { mc.levelRenderer.allChanged() }
        }

        mc.options.cameraType = perspective ?: return

        if (config.staticView) {
            mc.options.fovEffectScale.set(fovScale)
            mc.options.bobView.set(bobView)
        }

        perspective = null
        //isSneaking = false;
    }

    private fun unpress() {
        mc.options.keyUp.isDown = false
        mc.options.keyDown.isDown = false
        mc.options.keyRight.isDown = false
        mc.options.keyLeft.isDown = false
        mc.options.keyJump.isDown = false
        mc.options.keyShift.isDown = false
    }

    @EventHandle
    private fun onOpenScreen(event: ScreenOpenEvent) {
        unpress()

        prevPos.set(pos)
        lastYaw = yaw
        lastPitch = pitch
    }

    @EventHandle
    private fun onTick(event: TickEventPost) {
        Main.LOGGER.warn("Freecam onTick called")
        val cameraEntity = mc.cameraEntity ?: return
        if (cameraEntity.isInWall) cameraEntity.noPhysics = true
        if (perspective == null) return
        if (!perspective!!.isFirstPerson) mc.options.cameraType = CameraType.FIRST_PERSON

        val forward = Vec3.directionFromRotation(0f, yaw)
        val right = Vec3.directionFromRotation(0f, yaw + 90)
        var velX = 0.0
        var velY = 0.0
        var velZ = 0.0

        var s = 0.5
        if (mc.options.keySprint.isDown) s = 1.0

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

    @EventHandle(priority = EventPriority.HIGH)
    fun onKey(event: KeyDownEvent) {
        if (event.keyCode == GLFW.GLFW_KEY_F3) return

        if (onInput(event.keyCode, KeyAction.Press)) event.cancel()
    }

    @EventHandle
    fun onKey(event: KeyUpEvent) {
        if (event.keyCode == GLFW.GLFW_KEY_F3) return

        if (onInput(event.keyCode, KeyAction.Release)) event.cancel()
    }

    @EventHandle(priority = EventPriority.HIGH)
    private fun onMouseClick(event: MouseClickEvent) {
        if (onInput(event.button(), event.action)) event.cancel()
    }

    fun getKey(bind: KeyMapping): Int {
        return (bind as KeyMappingAccessor).`somefrills$getKey`().value
    }

    private fun onInput(key: Int, action: KeyAction): Boolean {
        when (key) {
            getKey(mc.options.keyUp) -> {
                forward = action != KeyAction.Release
                mc.options.keyUp.isDown = false
            }

            getKey(mc.options.keyDown) -> {
                backward = action != KeyAction.Release
                mc.options.keyDown.isDown = false
            }

            getKey(mc.options.keyRight) -> {
                right = action != KeyAction.Release
                mc.options.keyRight.isDown = false
            }

            getKey(mc.options.keyLeft) -> {
                left = action != KeyAction.Release
                mc.options.keyLeft.isDown = false
            }

            getKey(mc.options.keyJump) -> {
                up = action != KeyAction.Release
                mc.options.keyJump.isDown = false
            }

            getKey(mc.options.keyShift) -> {
                down = action != KeyAction.Release
                mc.options.keyShift.isDown = false
            }

            else -> {
                return false
            }
        }

        return true
    }

    @EventHandle(priority = EventPriority.LOW)
    private fun onMouseScroll(event: MouseScrollEvent) {
        if (config.speedScrollSensitivity > 0 && mc.screen == null) {
            config.speed += event.value * 0.25 * (config.speedScrollSensitivity * config.speed)
            if (config.speed < 0.1) config.speed = 0.1

            event.cancel()
        }
    }


    @EventHandle
    private fun onChunkOcclusion(event: ChunkOcclusionEvent) {
        event.cancel()
    }

    @EventHandle
    private fun onGameLeft(event: GameStopEvent) {
        toggle()
    }

    @EventHandle
    private fun onPacketReceive(event: ReceivePacketEvent) {
        val packet = event.packet
        if (packet is ClientboundPlayerCombatKillPacket) {
            val entity = mc.level?.getEntity(packet.playerId()) ?: return
            if (entity === mc.player && config.toggleOnDeath) {
                toggle()
                ChatUtils.info("Toggled off because you died.")
            }
        } else if (packet is ClientboundSetHealthPacket) {
            val player = mc.player ?: return
            if (player.health - packet.health > 0 && config.toggleOnDamage) {
                toggle()
                ChatUtils.info("Toggled off because you took damage.")
            }
        } else if (packet is ClientboundRespawnPacket) {
            toggle()
            ChatUtils.info("Toggled off because you changed dimensions.")
        }
    }

    @EventHandle
    fun onInteractBlock(event: InteractBlockEvent) {
        event.cancel()
    }

    @EventHandle
    fun onInteractEntity(event: InteractEntityEvent) {
        event.cancel()
    }

    @EventHandle(priority = EventPriority.HIGH)
    fun onAttackEntity(event: AttackEntityEvent) {
        event.cancel()
    }

    @EventHandle(priority = EventPriority.HIGH)
    private fun onStartBreakingBlockEvent(event: StartBreakingBlockEvent) {
        event.cancel()
    }

    fun getX(tickDelta: Float): Double {
        return Mth.lerp(tickDelta.toDouble(), prevPos.x, pos.x)
    }

    fun getY(tickDelta: Float): Double {
        return Mth.lerp(tickDelta.toDouble(), prevPos.y, pos.y)
    }

    fun getZ(tickDelta: Float): Double {
        return Mth.lerp(tickDelta.toDouble(), prevPos.z, pos.z)
    }

    fun getYaw(tickDelta: Float): Double {
        return Mth.lerp(tickDelta, lastYaw, yaw).toDouble()
    }

    fun getPitch(tickDelta: Float): Double {
        return Mth.lerp(tickDelta, lastPitch, pitch).toDouble()
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

        pitch = Mth.clamp(pitch, -90f, 90f)
    }
}