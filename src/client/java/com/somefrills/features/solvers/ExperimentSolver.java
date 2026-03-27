package com.somefrills.features.solvers;

import com.somefrills.config.Feature;
import com.somefrills.config.SettingBool;
import com.somefrills.config.SettingInt;
import com.somefrills.events.ScreenOpenEvent;
import com.somefrills.events.SlotUpdateEvent;
import com.somefrills.events.WorldTickEvent;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.somefrills.Main.LOGGER;
import static com.somefrills.Main.mc;

// descriptions now provided in constructors

public class ExperimentSolver {
    public static final Feature instance = new Feature();

    public static final SettingBool chronomatron = new SettingBool(true, "Automatically solve the Chronomatron");
    public static final SettingBool ultrasequencer = new SettingBool(true, "Automatically solve the Ultrasequencer");
    public static final SettingInt clickDelay = new SettingInt(400, "Click delay");
    // Close menu settings: enable automatic closing and configurable thresholds.
    // The numeric threshold means "N" where closing occurs after N-1 clicks have been completed.
    public static final SettingBool closeOnChronomatronThreshold = new SettingBool(true, "Close menu when Chronomatron reaches threshold");
    public static final SettingInt chronomatronThreshold = new SettingInt(10, "Chronomatron close threshold (N means close after N-1 clicks)");
    public static final SettingBool closeOnUltrasequencerThreshold = new SettingBool(true, "Close menu when Ultrasequencer reaches threshold");
    public static final SettingInt ultrasequencerThreshold = new SettingInt(7, "Ultrasequencer close threshold (N means close after N-1 clicks)");

    // --- Chronomatron state ---
    private static final List<Slot> chronoSequence = new ArrayList<>();
    private static int nextClickIndex = 0;
    private static boolean rememberPhase = true;
    private static int currentRoundProgress = 0;

    // --- Ultrasequencer state ---
    private static final List<Solution> ultraSolution = new ArrayList<>();
    private static long lastClickTime = 0;
    private static int ultraSolutionInitialSize = 0;

    private static void updatePhase(ItemStack stack) {
        Item item = stack.getItem();

        if (!rememberPhase && item.equals(Items.GLOWSTONE)) {
            rememberPhase = true;
            currentRoundProgress = 0;
        }

        if (rememberPhase && item.equals(Items.CLOCK)) {
            rememberPhase = false;
        }
    }

    public static ExperimentType getExperimentType() {
        if (Utils.isOnPrivateIsland() && mc.currentScreen instanceof GenericContainerScreen container) {
            String title = container.getTitle().getString();
            if (title.startsWith("Chronomatron (")) return ExperimentType.Chronomatron;
            if (title.startsWith("Ultrasequencer (")) return ExperimentType.Ultrasequencer;
        }
        return ExperimentType.None;
    }

    private static boolean isDye(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof DyeItem
                || item.equals(Items.INK_SAC)
                || item.equals(Items.BONE_MEAL)
                || item.equals(Items.LAPIS_LAZULI)
                || item.equals(Items.COCOA_BEANS);
    }

    private static boolean isTerracotta(ItemStack stack) {
        return stack.getItem().toString().endsWith("terracotta");
    }

    private static boolean isGlowstone(ItemStack stack) {
        return stack.getItem().equals(Items.GLOWSTONE);
    }

    // Your filter
    // 33 & 42
    // 12 & 21
    // Should only allow the 12 and 33
    private static boolean isValidChronoSlot(int idx) {
        return (11 <= idx && idx <= 19) || (30 <= idx && idx <= 38);
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (!instance.isActive() || rememberPhase) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < clickDelay.value()) return;

        ExperimentType type = getExperimentType();

        // --- Chronomatron solver ---
        if (chronomatron.value() && type == ExperimentType.Chronomatron && !chronoSequence.isEmpty()) {
            if (nextClickIndex < chronoSequence.size()) {
                Slot slotToClick = chronoSequence.get(nextClickIndex);
                Utils.clickSlot(slotToClick.id);
                lastClickTime = currentTime;

                LOGGER.info("[Chronomatron] Clicked slot ID: {} | Next index: {}/{}",
                        slotToClick.id, nextClickIndex + 1, chronoSequence.size());

                nextClickIndex++;
                // If configured, close the menu when we have reached the configured amount
                // (user provides N meaning close after N-1 clicks)
                try {
                    if (closeOnChronomatronThreshold.value()) {
                        int threshold = Math.max(1, chronomatronThreshold.value());
                        if (nextClickIndex >= threshold - 1) {
                            if (mc.player != null) mc.player.closeHandledScreen();
                        }
                    }
                } catch (Throwable ignored) {
                }
            }
        }

        // --- Ultrasequencer solver ---
        if (ultrasequencer.value() && type == ExperimentType.Ultrasequencer && !ultraSolution.isEmpty()) {
            Slot slotToClick = ultraSolution.getFirst().slot;
            Utils.clickSlot(slotToClick.id);
            ultraSolution.removeFirst();
            lastClickTime = currentTime;
            // initialize initial size if not set
            if (ultraSolutionInitialSize == 0) ultraSolutionInitialSize = Math.max(ultraSolution.size() + 1, 1);
            int clicksDone = ultraSolutionInitialSize - ultraSolution.size();
            // If configured, close the menu once we've clicked enough items
            try {
                if (closeOnUltrasequencerThreshold.value()) {
                    int threshold = Math.max(1, ultrasequencerThreshold.value());
                    if (clicksDone >= threshold - 1) {
                        if (mc.player != null) mc.player.closeHandledScreen();
                        ultraSolutionInitialSize = 0;
                    }
                }
            } catch (Throwable ignored) {
            }
            if (ultraSolution.isEmpty()) ultraSolutionInitialSize = 0;
        }
    }

    @EventHandler
    private static void onSlotUpdate(SlotUpdateEvent event) {
        if (!instance.isActive() || event.isInventory || event.slot == null) return;

        ExperimentType experimentType = getExperimentType();
        if (experimentType == ExperimentType.None) return;

        updatePhase(event.stack);

        // --- Chronomatron recording ---
        if (chronomatron.value() && experimentType == ExperimentType.Chronomatron && rememberPhase) {

            if (isTerracotta(event.stack) && isValidChronoSlot(event.slot.id)) {

                currentRoundProgress++;

                // Only add NEW element (the +1 each round)
                if (currentRoundProgress > chronoSequence.size()) {
                    chronoSequence.add(event.slot);

                    LOGGER.info("[Chronomatron] Added NEW slot ID: {} | Sequence length: {}",
                            event.slot.id, chronoSequence.size());
                } else {
                    LOGGER.info("[Chronomatron] Ignored replay slot ID: {}", event.slot.id);
                }
            }

            if (isGlowstone(event.stack)) {
                if (!chronoSequence.isEmpty()) {
                    nextClickIndex = 0;
                    currentRoundProgress = 0;

                    LOGGER.info("[Chronomatron] Round finished. Full sequence length: {}",
                            chronoSequence.size());
                }
            }
        }

        // --- Ultrasequencer recording ---
        if (ultrasequencer.value() && experimentType == ExperimentType.Ultrasequencer) {
            if (isGlowstone(event.stack)) {
                List<Solution> tempSolution = new ArrayList<>();
                for (Slot slot : Utils.getContainerSlots(event.handler)) {
                    if (isDye(slot.getStack())) {
                        tempSolution.add(new Solution(slot.getStack(), slot));
                    }
                }
                tempSolution.sort(Comparator.comparingInt(s -> s.stack.getCount()));
                ultraSolution.clear();
                ultraSolution.addAll(tempSolution);
                // initialize/reset ultrasequencer initial size when a new solution appears
                ultraSolutionInitialSize = ultraSolution.size();
            }
        }
    }

    @EventHandler
    private static void onScreen(ScreenOpenEvent event) {
        if (instance.isActive()) {
            rememberPhase = true;
            chronoSequence.clear();
            nextClickIndex = 0;
            currentRoundProgress = 0;

            ultraSolution.clear();
            ultraSolutionInitialSize = 0;
            lastClickTime = System.currentTimeMillis();

            LOGGER.info("[Chronomatron] Screen opened, sequence cleared");
        }
    }

    public enum ExperimentType {
        Chronomatron, Ultrasequencer, None
    }

    private static class Solution {
        public ItemStack stack;
        public Slot slot;

        public Solution(ItemStack stack, Slot slot) {
            this.stack = stack;
            this.slot = slot;
        }
    }
}