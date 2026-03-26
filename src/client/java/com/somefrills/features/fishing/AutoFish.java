package com.somefrills.features.fishing;

import com.somefrills.config.Feature;
import com.somefrills.config.SettingBool;
import com.somefrills.config.SettingDescription;
import com.somefrills.config.SettingInt;
import com.somefrills.events.*;
import com.somefrills.misc.Clock;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;

import static com.somefrills.Main.mc;

public final class AutoFish {
    public static final Feature instance = new Feature("autoFish");

    @SettingDescription("Use fish timer checks from armor stand names")
    public static final SettingBool useFishTimerCheck = new SettingBool(true);
    @SettingDescription("Automatically catch fish")
    public static final SettingBool autoCatch = new SettingBool(true);
    @SettingDescription("Delay before auto catching (ms)")
    public static final SettingInt autoCatchDelay = new SettingInt(0);
    @SettingDescription("Automatically throw the rod after catch")
    public static final SettingBool autoThrow = new SettingBool(true);
    @SettingDescription("Delay before auto throwing (ms)")
    public static final SettingInt autoThrowDelay = new SettingInt(100);
    @SettingDescription("Initial delay before first auto throw (ms)")
    public static final SettingInt autoThrowFirstDelay = new SettingInt(500);
    @SettingDescription("Do not wait for hook dead when auto throwing")
    public static final SettingBool doNotWaitHookDead = new SettingBool(false);

    // Fault-detection settings (made configurable)
    @SettingDescription("Enable AutoFish fault detection (prevents infinite recast loops)")
    public static final SettingBool faultDetectionEnabled = new SettingBool(true);
    @SettingDescription("How many milliseconds to wait for the bobber to appear before auto-recasting")
    public static final SettingInt hookWaitTimeoutMs = new SettingInt(1500);
    @SettingDescription("Maximum automatic recast attempts before giving up")
    public static final SettingInt maxRecastAttempts = new SettingInt(3);
    @SettingDescription("Cooldown in milliseconds between automatic recasts")
    public static final SettingInt recastCooldownMs = new SettingInt(500);

    @SettingDescription("Verbose logging for AutoFish")
    public static final SettingBool verbose = new SettingBool(false);

    public static int counter = 0;
    // runtime state (moved to top)
    private static ItemStack heldItem = ItemStack.EMPTY;
    private static boolean heldRod = false;
    private static long waitingForHookSince = 0;
    private static boolean waitingForHookCast = false;
    private static int hookId = -1; // -1 means none

    // hook waiting state is never null; use NONE as the default/idle state
    private static HookWaitingState hookWaitingState = HookWaitingState.NONE;
    private static Clock catchClock = new Clock();
    private static Clock throwClock = new Clock();
    private static Clock throwFirstClock = new Clock();
    private static boolean doneRightClickThisTick = false;
    // Fault detection / anti-infinite-recast (runtime trackers)
    private static long lastUserInteractTime = 0; // last time user (or any interact event) occurred
    private static int recastAttempts = 0; // current consecutive auto recast attempts
    private static long lastRecastTime = 0; // last auto recast timestamp

    static void reset() {
        heldItem = ItemStack.EMPTY;
        heldRod = false;
        doneRightClickThisTick = false;
        catchClock = new Clock();
        throwClock = new Clock();
        throwFirstClock = new Clock();
        hookWaitingState = HookWaitingState.NONE;
        hookId = -1;
        // reset fault detection
        waitingForHookSince = 0;
        waitingForHookCast = false;
        recastAttempts = 0;
        lastRecastTime = 0;
    }

    @EventHandler
    public static void onWorldLoad(ServerJoinEvent event) {
        hookId = -1;
        heldItem = ItemStack.EMPTY;
        hookWaitingState = HookWaitingState.NONE;

        if (instance.isActive()) {
            throwFirstClock.update();
            reset();
        }

        AutoFishAntiAfk.reset();
    }

    private static boolean isHoldingRod(ItemStack item) {
        return Utils.getSkyblockId(item).contains("ROD");
    }

    @EventHandler
    public static void onWorldTick(WorldTickEvent event) {
        doneRightClickThisTick = false;

        var player = mc.player;
        if (player == null) return;
        var holdingItem = Utils.getHeldItem();
        if (heldItem != holdingItem) {
            boolean holdingRod = isHoldingRod(holdingItem);

            if (heldRod && !holdingRod) {
                AutoFishAntiAfk.reset();
                // Switching away from rod: clear only hook-related state, do NOT reset throwFirstClock
                heldRod = false;
                heldItem = holdingItem;
                doneRightClickThisTick = false;
                hookWaitingState = HookWaitingState.NONE;
                hookId = -1;
                waitingForHookSince = 0;
                waitingForHookCast = false;
                recastAttempts = 0;
                return;
            }
            // Only update throwFirstClock when switching TO a rod
            if (!heldRod && holdingRod) {
                throwFirstClock.update();
            }
            heldRod = holdingRod;
            heldItem = holdingItem;
        }
        if (!heldRod) {
            // Already handled above, but keep as safety
            return;
        }
        // If we are waiting for a hook after cast,
        // and it's been more than HOOK_WAIT_TIMEOUT_MS, consider recasting
        if (faultDetectionEnabled.value() && waitingForHookCast && waitingForHookSince > 0 && hookId == -1) {
            long now = System.currentTimeMillis();
            if (now - waitingForHookSince > hookWaitTimeoutMs.value()) {
                // If user interacted recently, assume user caused the change and bail out
                if (now - lastUserInteractTime < 500) {
                    chat("User interaction detected; skipping recast");
                    waitingForHookSince = 0;
                    waitingForHookCast = false;
                    recastAttempts = 0;
                } else if (recastAttempts >= maxRecastAttempts.value()) {
                    chat("Max recast attempts reached; not recasting automatically");
                    waitingForHookSince = 0;
                    waitingForHookCast = false;
                    recastAttempts = 0;
                } else if (now - lastRecastTime < recastCooldownMs.value()) {
                    // too soon since last recast, skip this tick (cooldown)
                    chat("Recast cooldown active: {}ms left", Math.max(0, recastCooldownMs.value() - (now - lastRecastTime)));
                } else {
                    chat("No hook detected after {}ms — recasting (attempt)", now - waitingForHookSince);
                    doRightClick();
                    recastAttempts++;
                    lastRecastTime = now;
                    // reset the waiting timer to allow some time for the hook to appear
                    waitingForHookSince = now;
                }
            }
        }
        if (hookId != -1) {
            waitingForHookSince = 0; // reset if hook appears
            waitingForHookCast = false;
            recastAttempts = 0;
            lastRecastTime = 0;
        }
        if (instance.isActive()) doThrow();
    }

    @EventHandler
    public static void onInteractItem(InteractItemEvent event) {
        // mark that we did a right-click this tick
        doneRightClickThisTick = true;
        lastUserInteractTime = System.currentTimeMillis();
    }

    public static boolean matchFishingTimer(String str) {
        if (str == null) return false;
        return str.contains("!!!");
    }

    private static void doRightClick() {
        if (doneRightClickThisTick) return;
        var player = mc.player;
        if (player == null) return;
        if (mc.gameMode == null) return;
        mc.gameMode.useItem(player, InteractionHand.MAIN_HAND);
        doneRightClickThisTick = true;
    }

    @EventHandler
    private static void onEntity(EntityUpdatedEvent event) {
        var entity = event.entity;
        if (!(entity instanceof FishingHook hook)) return;
        var player = mc.player;
        if (player == null || !heldRod || hook.getOwner() == null) return;
        if (!hook.getOwner().getUUID().equals(player.getUUID())) {
            return;
        }
        hookId = hook.getId();
        hookWaitingState = HookWaitingState.NONE;
        waitingForHookSince = 0; // reset waiting when hook joins
        waitingForHookCast = false;
        // reset fault/detection
        recastAttempts = 0;
        lastRecastTime = 0;
    }

    @EventHandler
    public static void onEntityDead(EntityRemovedEvent event) {
        int id = event.entity.getId();
        if (hookId != -1 && id == hookId) {
            counter++;
            if (counter < 2) {
                return;
            }
            counter = 0;
            hookId = -1;
            if (hookWaitingState != HookWaitingState.WAITING_JOIN) hookWaitingState = HookWaitingState.NONE;
        }
        // timerRef removed — no-op
    }

    @EventHandler
    public static void onS1CPacket(EntityNamedEvent event) {
        if (!useFishTimerCheck.value()) return;
        try {
            // use event.namePlain (already sanitized) to avoid NPEs from getCustomName()
            if (instance.isActive() && matchFishingTimer(event.namePlain)) {
                chat("Active fishing timer catch");
                doCatch();
            }

        } catch (Exception ignored) {
        }
    }

    private static void doCatch() {
        if (
                !autoCatch.value() ||
                        doneRightClickThisTick ||
                        !heldRod ||
                        hookId == -1 ||
                        hookWaitingState != HookWaitingState.NONE ||
                        !catchClock.ended(autoCatchDelay.value())
        ) return;
        chat("Do catch, time={}", System.currentTimeMillis());
        doRightClick();
        hookWaitingState = HookWaitingState.WAITING_DEAD;
        throwClock.update();
        AutoFishAntiAfk.trigger();
        // Reset waiting for hook creation after reel
        waitingForHookSince = 0;
        waitingForHookCast = false;
    }

    private static void doThrow() {
        if (
                !autoThrow.value() ||
                        doneRightClickThisTick ||
                        !heldRod ||
                        hookWaitingState == HookWaitingState.WAITING_JOIN ||
                        hookId != -1 && !(doNotWaitHookDead.value() && hookWaitingState == HookWaitingState.WAITING_DEAD) ||
                        !throwClock.ended(autoThrowDelay.value()) ||
                        !throwFirstClock.ended(autoThrowFirstDelay.value())
        ) return;
        chat("Do throw, time={}", System.currentTimeMillis());
        doRightClick();
        hookWaitingState = HookWaitingState.WAITING_JOIN;
        catchClock.update();
        // Start waiting for hook creation after cast
        waitingForHookSince = System.currentTimeMillis();
        waitingForHookCast = true;
        // fault-detection bookkeeping
        recastAttempts = 0;
        lastRecastTime = 0;
    }

    private static void chat(String str) {
        if (!verbose.value()) return;
        if (str == null || str.isEmpty()) return;
        Utils.info(str);
    }

    private static void chat(String fmt, Object... args) {
        if (!verbose.value()) return;
        if (fmt == null || fmt.isEmpty()) return;
        Utils.infoFormat(fmt, args);
    }

    private enum HookWaitingState {
        NONE,
        WAITING_JOIN,
        WAITING_DEAD,
    }
}
