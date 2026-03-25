package com.somefrills.features.farming;

import com.somefrills.config.Feature;
import com.somefrills.config.SettingDescription;
import com.somefrills.config.SettingKeybind;
import com.somefrills.events.InputEvent;
import com.somefrills.events.ScreenOpenEvent;
import com.somefrills.events.ServerJoinEvent;
import com.somefrills.events.WorldTickEvent;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

import static com.somefrills.Main.mc;

public class AutoFarm {
    public static final Feature instance = new Feature("autoFarm");
    @SettingDescription("Toggle AutoFarm on/off")
    public static SettingKeybind toggleKey = new SettingKeybind(GLFW.GLFW_KEY_GRAVE_ACCENT);

    // tuning: yaw/pitch speed bounds (per tick)
    private static final float MIN_YAW_SPEED = 2.0f;   // slowest per-tick movement
    private static final float MAX_YAW_SPEED = 10.0f;  // fastest per-tick movement
    private static final float MIN_PITCH_SPEED = 1.0f;
    private static final float MAX_PITCH_SPEED = 4.0f;
    private static final float REACHED_EPSILON = 1.0f; // considered at target when within this many degrees
    private static final Random RAND = new Random();
    private static final long STATE_COOLDOWN_MS = 150; // 150ms debounce
    // velocity threshold to consider we're blocked on an axis
    private static final double BLOCK_VELOCITY_EPS = 0.02; // small horizontal speed considered blocked
    // runtime state: whether we've reached the target facing already
    private static boolean applied = false;
    // last computed target yaw so we can re-apply when it changes
    private static float lastTargetYaw = Float.NaN;
    private static MoveState moveState = MoveState.DIAG_RIGHT;
    // preference toggle for alternating sides when forward is blocked
    private static boolean preferRight = true;
    // timestamps to debounce state switches
    private static long lastStateChangeMs = 0;
    private static boolean isActive = false;

    @EventHandler
    private static void onServerJoin(ServerJoinEvent event) {
        applied = false;
        lastTargetYaw = Float.NaN;
    }

    @EventHandler
    public static void onWorldTick(WorldTickEvent event) {
        var player = mc.player;
        if (player == null) return;
        if(!isActive) return;
        if (!Utils.isOnGardenPlot()) return;

        {
            String skyblockId = Utils.getSkyblockId(Utils.getHeldItem()).toUpperCase();
            if (!skyblockId.contains("WHEAT") || !skyblockId.contains("HOE")) {
                applied = false;
                lastTargetYaw = Float.NaN;
                // release all keys explicitly
                setMovementKeys(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE);
                return;
            }
        }

        // compact mc.options checks (requested)
        if (mc.options == null) return;
        var options = mc.options;
        if (options.attackKey == null || options.forwardKey == null || options.rightKey == null || options.leftKey == null)
            return;

        float targetYaw = getTargetYaw(player);
        // If target changed significantly, mark not applied so we interpolate to new target
        if (Float.isNaN(lastTargetYaw) || Math.abs(angleDiff(targetYaw, lastTargetYaw)) > 1.0f) {
            applied = false;
            lastTargetYaw = targetYaw;
        }

        if (!applied) {
            boolean reached = interpolateFacing(player, targetYaw);
            if (reached) {
                applied = true;
                moveState = MoveState.DIAG_RIGHT;
                preferRight = true;
                lastStateChangeMs = System.currentTimeMillis();
            }
            return; // still orienting -> don't run movement logic this tick
        }

        // --- movement logic: zig-zag while breaking ---
        boolean pressForward = false;
        boolean pressRight = false;
        boolean pressLeft = false;

        long now = System.currentTimeMillis();
        Vec3d vel = player.getVelocity();

        // compute local forward/right components of velocity based on player yaw
        double rad = Math.toRadians(player.getYaw());
        double forwardX = -Math.sin(rad);
        double forwardZ = Math.cos(rad);
        double rightX = Math.cos(rad);
        double rightZ = Math.sin(rad);

        double forwardVel = vel.x * forwardX + vel.z * forwardZ;
        double rightVel = vel.x * rightX + vel.z * rightZ;

        // state transitions with debounce
        switch (moveState) {
            case DIAG_RIGHT -> {
                pressForward = true;
                pressRight = true;
                // if right blocked (we are pressing right but rightVel too small or negative), switch to forward-only
                if ((rightVel <= BLOCK_VELOCITY_EPS) && now - lastStateChangeMs > STATE_COOLDOWN_MS) {
                    moveState = MoveState.FORWARD;
                    lastStateChangeMs = now;
                }
            }
            case FORWARD -> {
                pressForward = true;
                // if forward blocked, switch to a diagonal based on preference
                if ((forwardVel <= BLOCK_VELOCITY_EPS) && now - lastStateChangeMs > STATE_COOLDOWN_MS) {
                    moveState = preferRight ? MoveState.DIAG_RIGHT : MoveState.DIAG_LEFT;
                    // flip preference for next time
                    preferRight = !preferRight;
                    lastStateChangeMs = now;
                }
            }
            case DIAG_LEFT -> {
                pressForward = true;
                pressLeft = true;
                // if left blocked (expect rightVel negative; left blocked if rightVel >= -eps), switch to forward-only
                if ((rightVel >= -BLOCK_VELOCITY_EPS) && now - lastStateChangeMs > STATE_COOLDOWN_MS) {
                    moveState = MoveState.FORWARD;
                    lastStateChangeMs = now;
                }
            }
        }

        // Apply key presses (including attack) — only change the keys passed; nullable Boolean allows "don't touch" semantics
        setMovementKeys(pressForward, pressRight, pressLeft, Boolean.TRUE);
    }

    // Helper: set the movement & attack key pressed state. Parameters are boxed Booleans where null means "leave that key unchanged".
    private static void setMovementKeys(Boolean forward, Boolean right, Boolean left, Boolean attack) {
        if (mc.options == null) return;
        var options = mc.options;
        if (options.attackKey == null || options.forwardKey == null || options.rightKey == null || options.leftKey == null)
            return;
        if (forward != null) options.forwardKey.setPressed(forward);
        if (right != null) options.rightKey.setPressed(right);
        if (left != null) options.leftKey.setPressed(left);
        if (attack != null) options.attackKey.setPressed(attack);
    }

    // Extracted facing interpolation logic. Returns true when we've reached the target facing (both yaw and pitch).
    private static boolean interpolateFacing(ClientPlayerEntity player, float targetYaw) {
        float currentYaw = player.getYaw();
        float yawDelta = angleDiff(targetYaw, currentYaw);
        float absYawDelta = Math.abs(yawDelta);

        float proportional = absYawDelta * 0.22f;
        float base = 3.0f;
        float jitter = 0.85f + RAND.nextFloat() * 0.3f;
        float desiredSpeed = (base + proportional) * jitter;
        desiredSpeed = Math.max(MIN_YAW_SPEED, Math.min(MAX_YAW_SPEED, desiredSpeed));
        float yawStep = Math.signum(yawDelta) * Math.min(desiredSpeed, absYawDelta);
        float newYaw = currentYaw + yawStep;

        float currentPitch = player.getPitch();
        float pitchDelta = 0f - currentPitch;
        float absPitchDelta = Math.abs(pitchDelta);
        float pProportional = absPitchDelta * 0.4f;
        float pBase = 1.2f;
        float pJitter = 0.9f + RAND.nextFloat() * 0.2f;
        float pDesired = (pBase + pProportional) * pJitter;
        pDesired = Math.max(MIN_PITCH_SPEED, Math.min(MAX_PITCH_SPEED, pDesired));
        float pitchStep = Math.signum(pitchDelta) * Math.min(pDesired, absPitchDelta);
        float newPitch = currentPitch + pitchStep;

        player.setYaw(newYaw);
        player.setPitch(newPitch);

        return Math.abs(angleDiff(targetYaw, newYaw)) <= REACHED_EPSILON && Math.abs(newPitch - 0f) <= REACHED_EPSILON;
    }

    private static float getTargetYaw(ClientPlayerEntity player) {
        float yaw = player.getYaw();
        // normalize to [0,360)
        float normalized = yaw % 360f;
        if (normalized < 0) normalized += 360f;

        // candidate index around current yaw
        int cand = Math.round(normalized / 45f);
        // ensure cand is odd
        if (cand % 2 == 0) {
            // choose the closer of cand-1 and cand+1
            int low = cand - 1;
            int high = cand + 1;
            float lowDiff = Math.abs(normalized - low * 45f);
            float highDiff = Math.abs(normalized - high * 45f);
            cand = (lowDiff <= highDiff) ? low : high;
        }
        float targetYaw = (cand * 45f) % 360f;
        // convert to range used by setYaw (center to [-180,180])
        if (targetYaw > 180f) targetYaw -= 360f;
        return targetYaw;
    }

    /**
     * Shortest signed angular difference from "from" to "to" in degrees in range (-180,180].
     * i.e., how much you need to add to "from" to get to "to" via the shortest path.
     */
    private static float angleDiff(float to, float from) {
        float diff = to - from;
        while (diff <= -180f) diff += 360f;
        while (diff > 180f) diff -= 360f;
        return diff;
    }

    @EventHandler
    public static void onScreenOpen(ScreenOpenEvent event) {
        // if a screen opens, clear applied state so the facing can be reapplied when returning
        if (applied) applied = false;
        lastTargetYaw = Float.NaN;
        // release keys
        setMovementKeys(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE);
    }

    @EventHandler
    public static void onKey(InputEvent event) {
        // toggle key handling: on press, toggle feature enabled state
        if (!(event.isKeyboard && toggleKey.isKey(event.key) && event.action == GLFW.GLFW_PRESS)) {
            return;
        }
        boolean newState = !isActive;
        isActive = newState;
        if (!newState) {
            // disabled -> clear applied and release keys
            applied = false;
            lastTargetYaw = Float.NaN;
            setMovementKeys(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE);
        }
        Utils.infoFormat("AutoFarm {}", newState ? "enabled" : "disabled");
        event.cancel();
    }

    // Zig-zag movement state machine
    private enum MoveState {DIAG_RIGHT, FORWARD, DIAG_LEFT}
}
