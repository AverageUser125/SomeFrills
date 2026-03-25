package com.somefrills.features.farming;

import com.somefrills.config.Feature;
import com.somefrills.events.ScreenOpenEvent;
import com.somefrills.events.WorldTickEvent;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

import static com.somefrills.Main.mc;

public class AutoFarm {
    public static final Feature instance = new Feature("autoFarm");

    // runtime state: whether we've reached the target facing already
    private static boolean applied = false;
    // last computed target yaw so we can re-apply when it changes
    private static float lastTargetYaw = Float.NaN;

    // tuning: yaw/pitch speed bounds (per tick)
    private static final float MIN_YAW_SPEED = 2.0f;   // slowest per-tick movement
    private static final float MAX_YAW_SPEED = 10.0f;  // fastest per-tick movement
    private static final float MIN_PITCH_SPEED = 1.0f;
    private static final float MAX_PITCH_SPEED = 4.0f;
    private static final float REACHED_EPSILON = 1.0f; // considered at target when within this many degrees

    private static final Random RAND = new Random();

    // Zig-zag movement state machine
    private enum MoveState { DIAG_RIGHT, FORWARD, DIAG_LEFT }
    private static MoveState moveState = MoveState.DIAG_RIGHT;
    // preference toggle for alternating sides when forward is blocked
    private static boolean preferRight = true;

    // timestamps to debounce state switches
    private static long lastStateChangeMs = 0;
    private static final long STATE_COOLDOWN_MS = 150; // 150ms debounce
    // velocity threshold to consider we're blocked on an axis
    private static final double BLOCK_VELOCITY_EPS = 0.02; // small horizontal speed considered blocked

    @EventHandler
    public static void onWorldTick(WorldTickEvent event) {
        var player = mc.player;
        if (player == null) return;
        // only run on garden plots
        if (!Utils.isOnGardenPlot()) return;

        ItemStack held = Utils.getHeldItem();
        boolean isHoe = held != null && !held.isEmpty() && held.getItem() instanceof HoeItem;
        boolean nameHasWheat = false;
        if (isHoe) nameHasWheat = Utils.getSkyblockId(held).contains("wheat");

        // pull out keybindings once to avoid repetitive null checks
        KeyBinding forwardKey = mc.options != null ? mc.options.forwardKey : null;
        KeyBinding rightKey = mc.options != null ? mc.options.rightKey : null;
        KeyBinding leftKey = mc.options != null ? mc.options.leftKey : null;
        KeyBinding attackKey = mc.options != null ? mc.options.attackKey : null;

        // early return when module inactive or item not held; release keys
        if (!instance.isActive() || !isHoe || !nameHasWheat) {
            applied = false;
            lastTargetYaw = Float.NaN;
            if (forwardKey != null) forwardKey.setPressed(false);
            if (rightKey != null) rightKey.setPressed(false);
            if (leftKey != null) leftKey.setPressed(false);
            if (attackKey != null) attackKey.setPressed(false);
            return;
        }

        float targetYaw = getTargetYaw(player);
        // (current yaw not needed here; interpolation uses the player object directly)

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
        if (attackKey != null) attackKey.setPressed(true);

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

        // Apply key presses to mc.options
        if (forwardKey != null) forwardKey.setPressed(pressForward);
        if (rightKey != null) rightKey.setPressed(pressRight);
        if (leftKey != null) leftKey.setPressed(pressLeft);
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
        KeyBinding forwardKey = mc.options != null ? mc.options.forwardKey : null;
        KeyBinding rightKey = mc.options != null ? mc.options.rightKey : null;
        KeyBinding leftKey = mc.options != null ? mc.options.leftKey : null;
        KeyBinding attackKey = mc.options != null ? mc.options.attackKey : null;
        if (forwardKey != null) forwardKey.setPressed(false);
        if (rightKey != null) rightKey.setPressed(false);
        if (leftKey != null) leftKey.setPressed(false);
        if (attackKey != null) attackKey.setPressed(false);
    }
}
