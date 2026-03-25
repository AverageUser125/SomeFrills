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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.glfw.GLFW;

import static com.somefrills.Main.mc;

public class AutoFarm {
    public static final Feature instance = new Feature("autoFarm");

    @SettingDescription("Toggle AutoFarm on/off")
    public static SettingKeybind toggleKey = new SettingKeybind(GLFW.GLFW_KEY_GRAVE_ACCENT);

    private static final float MIN_YAW_SPEED = 2.0f;
    private static final float MAX_YAW_SPEED = 10.0f;
    private static final float MIN_PITCH_SPEED = 1.0f;
    private static final float MAX_PITCH_SPEED = 4.0f;
    private static final float REACHED_EPSILON = 1.0f;
    private static final long STATE_COOLDOWN_MS = 150;
    // --- smooth key press ---
    private static float forwardPress = 0f;
    private static float sidePress = 0f;

    private static boolean applied = false;
    private static float lastTargetYaw = Float.NaN;

    private static MoveState moveState = MoveState.DIAG_RIGHT;
    private static long lastStateChangeMs = 0;

    private static boolean isActive = false;

    @EventHandler
    private static void onServerJoin(ServerJoinEvent event) {
        applied = false;
        lastTargetYaw = Float.NaN;
    }
    private static BlockPos getBlockOffset(ClientPlayerEntity player, double angleDegrees) {
        double yawRad = Math.toRadians(player.getYaw() + angleDegrees);
        int offsetX = (int)Math.round(Math.cos(yawRad));
        int offsetZ = (int)Math.round(Math.sin(yawRad));
        return player.getBlockPos().add(offsetX, 0, offsetZ);
    }
    private static boolean isSolidBlockAt(BlockPos pos) {
        var state = mc.world.getBlockState(pos);
        if(state == null) return false;
        return state.isLiquid();
    }

    @EventHandler
    public static void onWorldTick(WorldTickEvent event) {
        ClientPlayerEntity player = mc.player;
        if (player == null || !isActive || mc.world == null) return;
        if (!Utils.isOnGardenPlot()) return;

        // --- check held item ---
        String skyblockId = Utils.getSkyblockId(Utils.getHeldItem()).toUpperCase();
        if (!skyblockId.contains("WHEAT") || !skyblockId.contains("HOE")) {
            reset();
            return;
        }

        if (mc.options == null) return;
        var options = mc.options;
        if (options.attackKey == null || options.forwardKey == null ||
                options.rightKey == null || options.leftKey == null) return;

        // --- facing interpolation ---
        float targetYaw = getTargetYaw(player);
        if (Float.isNaN(lastTargetYaw) || Math.abs(angleDiff(targetYaw, lastTargetYaw)) > 1.0f) {
            applied = false;
            lastTargetYaw = targetYaw;
        }

        if (!applied) {
            if (interpolateFacing(player, targetYaw)) {
                applied = true;
                moveState = MoveState.DIAG_RIGHT;
                lastStateChangeMs = System.currentTimeMillis();
            }
            return; // still orienting
        }

        long now = System.currentTimeMillis();

        // --- compute block positions relative to player ---
        BlockPos diagRightPos = getBlockOffset(player, 45);
        BlockPos diagLeftPos  = getBlockOffset(player, -135);
        BlockPos forwardPos   = getBlockOffset(player, -45);
        BlockPos behindPos    = getBlockOffset(player, 135); // for RETURN

        // --- collision detection ---
        boolean diagRightBlocked = isSolidBlockAt(diagRightPos);
        boolean diagLeftBlocked  = isSolidBlockAt(diagLeftPos);
        boolean diagForwardBlocked = isSolidBlockAt(forwardPos);
        boolean behindBlocked = isSolidBlockAt(behindPos);

        // --- movement state machine ---
        boolean pressForward = false;
        boolean pressRight = false;
        boolean pressLeft = false;

        switch (moveState) {
            case DIAG_RIGHT -> {
                pressForward = true;
                pressRight = true;

                if (diagRightBlocked && diagForwardBlocked && now - lastStateChangeMs > STATE_COOLDOWN_MS) {
                    moveState = MoveState.DIAG_LEFT;
                    lastStateChangeMs = now;
                    Utils.infoFormat("State -> DIAG_LEFT (forward & right blocked)");
                }
            }

            case DIAG_LEFT -> {
                pressForward = false; // ONLY LEFT
                pressRight = false;
                pressLeft = true;

                if (diagLeftBlocked && !diagForwardBlocked && now - lastStateChangeMs > STATE_COOLDOWN_MS) {
                    moveState = MoveState.DIAG_RIGHT;
                    lastStateChangeMs = now;
                    Utils.infoFormat("State -> DIAG_RIGHT (left blocked, forward free)");
                } else if (diagLeftBlocked && diagForwardBlocked && now - lastStateChangeMs > STATE_COOLDOWN_MS) {
                    moveState = MoveState.RETURN;
                    lastStateChangeMs = now;
                    Utils.infoFormat("State -> RETURN (left + forward blocked, stuck in corner)");
                }
            }

            case RETURN -> {
                // backward + right
                pressForward = false; // backwards is implied by not pressing forward
                pressRight = true;
                pressLeft = false;

                if (behindBlocked && now - lastStateChangeMs > STATE_COOLDOWN_MS) {
                    moveState = MoveState.DIAG_RIGHT;
                    lastStateChangeMs = now;
                    Utils.infoFormat("State -> DIAG_RIGHT (return trip finished, hit wall behind)");
                }
            }
        }

        // --- smooth key presses ---
        forwardPress += ((pressForward ? 1f : 0f) - forwardPress) * 0.25f;
        float targetSide = pressRight ? 1f : (pressLeft ? -1f : 0f);
        sidePress += (targetSide - sidePress) * 0.25f;

        options.forwardKey.setPressed(forwardPress > 0.1f);
        options.rightKey.setPressed(sidePress > 0.1f);
        options.leftKey.setPressed(sidePress < -0.1f);
        options.attackKey.setPressed(true);
    }
    private static void reset() {
        applied = false;
        lastTargetYaw = Float.NaN;
        forwardPress = 0;
        sidePress = 0;
        if (mc.options == null) return;
        var o = mc.options;
        o.forwardKey.setPressed(false);
        o.rightKey.setPressed(false);
        o.leftKey.setPressed(false);
        o.attackKey.setPressed(false);
    }

    private static boolean interpolateFacing(ClientPlayerEntity player, float targetYaw) {
        float currentYaw = player.getYaw();
        float yawDelta = angleDiff(targetYaw, currentYaw);

        float speed = Math.min(MAX_YAW_SPEED,
                Math.max(MIN_YAW_SPEED, Math.abs(yawDelta) * 0.22f + 3f));

        float newYaw = currentYaw + Math.signum(yawDelta) * Math.min(speed, Math.abs(yawDelta));

        float currentPitch = player.getPitch();
        float pitchDelta = -currentPitch;

        float pSpeed = Math.min(MAX_PITCH_SPEED,
                Math.max(MIN_PITCH_SPEED, Math.abs(pitchDelta) * 0.4f + 1.2f));

        float newPitch = currentPitch + Math.signum(pitchDelta) * Math.min(pSpeed, Math.abs(pitchDelta));

        player.setYaw(newYaw);
        player.setPitch(newPitch);

        return Math.abs(angleDiff(targetYaw, newYaw)) <= REACHED_EPSILON
                && Math.abs(newPitch) <= REACHED_EPSILON;
    }

    private static float getTargetYaw(ClientPlayerEntity player) {
        float yaw = player.getYaw();
        float normalized = yaw % 360f;
        if (normalized < 0) normalized += 360f;

        int cand = Math.round(normalized / 45f);
        if (cand % 2 == 0) {
            int low = cand - 1;
            int high = cand + 1;
            float lowDiff = Math.abs(normalized - low * 45f);
            float highDiff = Math.abs(normalized - high * 45f);
            cand = (lowDiff <= highDiff) ? low : high;
        }

        float targetYaw = (cand * 45f) % 360f;
        if (targetYaw > 180f) targetYaw -= 360f;

        return targetYaw;
    }

    private static float angleDiff(float to, float from) {
        float diff = to - from;
        while (diff <= -180f) diff += 360f;
        while (diff > 180f) diff -= 360f;
        return diff;
    }

    @EventHandler
    public static void onScreenOpen(ScreenOpenEvent event) {
        reset();
    }

    @EventHandler
    public static void onKey(InputEvent event) {
        if (!(event.isKeyboard && toggleKey.isKey(event.key) && event.action == GLFW.GLFW_PRESS)) return;

        isActive = !isActive;

        if (!isActive) reset();

        Utils.infoFormat("AutoFarm {}", isActive ? "enabled" : "disabled");
        event.cancel();
    }

    private enum MoveState {
        DIAG_RIGHT, DIAG_LEFT, RETURN
    }
}