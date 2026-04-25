package com.somefrills.features.misc;

import com.somefrills.config.FrillsConfig;
import com.somefrills.config.misc.FreecamConfig;
import com.somefrills.events.*;
import com.somefrills.features.core.ToggleFeature;
import com.somefrills.misc.Input;
import com.somefrills.misc.KeyAction;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;
import org.lwjgl.glfw.GLFW;

import static com.somefrills.Main.mc;
import static com.somefrills.misc.Utils.info;

public class Freecam extends ToggleFeature {
    private final FreecamConfig config;

    public final Vector3d pos = new Vector3d();
    public final Vector3d prevPos = new Vector3d();

    private Perspective perspective = null;

    public float yaw, pitch;
    public float lastYaw, lastPitch;

    private double fovScale;
    private boolean bobView;

    private boolean forward, backward, right, left, up, down;

    public Freecam() {
        super(FrillsConfig.instance.misc.freecam.enabled, FrillsConfig.instance.misc.freecam.keybind);
        config = FrillsConfig.instance.misc.freecam;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.options == null || mc.player == null || mc.gameRenderer == null || mc.gameRenderer.getCamera() == null) {
            return;
        }

        fovScale = mc.options.getFovEffectScale().getValue();
        bobView = mc.options.getBobView().getValue();
        if (config.staticView) {
            mc.options.getFovEffectScale().setValue((double) 0);
            mc.options.getBobView().setValue(false);
        }
        yaw = mc.player.getYaw();
        pitch = mc.player.getPitch();

        perspective = mc.options.getPerspective();
        config.speed = config.speed;

        Utils.set(pos, mc.gameRenderer.getCamera().getCameraPos());
        Utils.set(prevPos, mc.gameRenderer.getCamera().getCameraPos());

        if (mc.options.getPerspective() == Perspective.THIRD_PERSON_FRONT) {
            yaw += 180;
            pitch *= -1;
        }

        lastYaw = yaw;
        lastPitch = pitch;

        //isSneaking = mc.options.sneakKey.isPressed();

        forward = Input.isPressed(mc.options.forwardKey);
        backward = Input.isPressed(mc.options.backKey);
        right = Input.isPressed(mc.options.rightKey);
        left = Input.isPressed(mc.options.leftKey);
        up = Input.isPressed(mc.options.jumpKey);
        down = Input.isPressed(mc.options.sneakKey);

        unpress();
        if (config.reloadChunks) mc.worldRenderer.reload();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (perspective == null) return;
        if (config.reloadChunks) {
            mc.execute(mc.worldRenderer::reload);
        }

        mc.options.setPerspective(perspective);

        if (config.staticView) {
            mc.options.getFovEffectScale().setValue(fovScale);
            mc.options.getBobView().setValue(bobView);
        }

        perspective = null;
        //isSneaking = false;
    }

    private void unpress() {
        mc.options.forwardKey.setPressed(false);
        mc.options.backKey.setPressed(false);
        mc.options.rightKey.setPressed(false);
        mc.options.leftKey.setPressed(false);
        mc.options.jumpKey.setPressed(false);
        mc.options.sneakKey.setPressed(false);
    }

    @EventHandler
    private void onOpenScreen(ScreenOpenEvent event) {
        unpress();

        prevPos.set(pos);
        lastYaw = yaw;
        lastPitch = pitch;
    }

    @EventHandler
    private void onTick(TickEventPost event) {
        if (mc.getCameraEntity() == null) return;
        if (mc.getCameraEntity().isInsideWall()) mc.getCameraEntity().noClip = true;
        if (!perspective.isFirstPerson()) mc.options.setPerspective(Perspective.FIRST_PERSON);

        Vec3d forward = Vec3d.fromPolar(0, yaw);
        Vec3d right = Vec3d.fromPolar(0, yaw + 90);
        double velX = 0;
        double velY = 0;
        double velZ = 0;

        double s = 0.5;
        if (Input.isPressed(mc.options.sprintKey)) s = 1;

        boolean a = false;
        if (this.forward) {
            velX += forward.x * s * config.speed;
            velZ += forward.z * s * config.speed;
            a = true;
        }
        if (this.backward) {
            velX -= forward.x * s * config.speed;
            velZ -= forward.z * s * config.speed;
            a = true;
        }

        boolean b = false;
        if (this.right) {
            velX += right.x * s * config.speed;
            velZ += right.z * s * config.speed;
            b = true;
        }
        if (this.left) {
            velX -= right.x * s * config.speed;
            velZ -= right.z * s * config.speed;
            b = true;
        }

        if (a && b) {
            double diagonal = 1 / Math.sqrt(2);
            velX *= diagonal;
            velZ *= diagonal;
        }

        if (this.up) {
            velY += s * config.speed;
        }
        if (this.down) {
            velY -= s * config.speed;
        }

        prevPos.set(pos);
        pos.set(pos.x + velX, pos.y + velY, pos.z + velZ);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onKey(InputEvent event) {
        if (Input.isKeyPressed(GLFW.GLFW_KEY_F3)) return;

        if (onInput(event.key, event.action)) event.cancel();
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onMouseClick(MouseClickEvent event) {
        if (onInput(event.button(), event.action)) event.cancel();
    }

    private boolean onInput(int key, KeyAction action) {
        if (Input.getKey(mc.options.forwardKey) == key) {
            forward = action != KeyAction.Release;
            mc.options.forwardKey.setPressed(false);
        } else if (Input.getKey(mc.options.backKey) == key) {
            backward = action != KeyAction.Release;
            mc.options.backKey.setPressed(false);
        } else if (Input.getKey(mc.options.rightKey) == key) {
            right = action != KeyAction.Release;
            mc.options.rightKey.setPressed(false);
        } else if (Input.getKey(mc.options.leftKey) == key) {
            left = action != KeyAction.Release;
            mc.options.leftKey.setPressed(false);
        } else if (Input.getKey(mc.options.jumpKey) == key) {
            up = action != KeyAction.Release;
            mc.options.jumpKey.setPressed(false);
        } else if (Input.getKey(mc.options.sneakKey) == key) {
            down = action != KeyAction.Release;
            mc.options.sneakKey.setPressed(false);
        } else {
            return false;
        }

        return true;
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onMouseScroll(MouseScrollEvent event) {
        if (config.speedScrollSensitivity > 0 && mc.currentScreen == null) {
            config.speed += event.value * 0.25 * (config.speedScrollSensitivity * config.speed);
            if (config.speed < 0.1) config.speed = 0.1;

            event.cancel();
        }
    }


    @EventHandler
    private void onChunkOcclusion(ChunkOcclusionEvent event) {
        event.cancel();
    }

    @EventHandler
    private void onGameLeft(GameStopEvent event) {
        toggle();
    }

    @EventHandler
    private void onPacketReceive(ReceivePacketEvent event) {
        if (event.packet instanceof DeathMessageS2CPacket packet) {
            Entity entity = mc.world.getEntityById(packet.playerId());
            if (entity == mc.player && config.toggleOnDeath) {
                toggle();
                info("Toggled off because you died.");
            }
        } else if (event.packet instanceof HealthUpdateS2CPacket packet) {
            if (mc.player.getHealth() - packet.getHealth() > 0 && config.toggleOnDamage) {
                toggle();
                info("Toggled off because you took damage.");
            }
        } else if (event.packet instanceof PlayerRespawnS2CPacket) {
            toggle();
            info("Toggled off because you changed dimensions.");
        }
    }

    public double getX(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevPos.x, pos.x);
    }

    public double getY(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevPos.y, pos.y);
    }

    public double getZ(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevPos.z, pos.z);
    }

    public double getYaw(float tickDelta) {
        return MathHelper.lerp(tickDelta, lastYaw, yaw);
    }

    public double getPitch(float tickDelta) {
        return MathHelper.lerp(tickDelta, lastPitch, pitch);
    }

    public boolean shouldRenderHands() {
        return config.renderHands;
    }

    public void changeLookDirection(double deltaX, double deltaY) {
        lastYaw = yaw;
        lastPitch = pitch;

        yaw += (float) deltaX;
        pitch += (float) deltaY;

        pitch = MathHelper.clamp(pitch, -90, 90);
    }
}
