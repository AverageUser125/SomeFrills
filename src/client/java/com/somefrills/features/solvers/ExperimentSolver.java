package com.somefrills.features.solvers;

import com.somefrills.config.Feature;
import com.somefrills.config.FrillsConfig;
import com.somefrills.config.solvers.SolverCategory.ExperimentSolverConfig;
import com.somefrills.events.ScreenOpenEvent;
import com.somefrills.events.SlotUpdateEvent;
import com.somefrills.events.WorldTickEvent;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.SlotOptions;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;

import java.util.*;

import static com.somefrills.Main.LOGGER;
import static com.somefrills.Main.mc;

public class ExperimentSolver extends Feature {
    private final ExperimentSolverConfig config;

    // --- Chronomatron state ---
    private final List<Slot> chronoSequence = new ArrayList<>();
    private int nextClickIndex = 0;
    private int currentRoundProgress = 0;
    // --- Ultrasequencer state ---
    private final List<Solution> ultraSolution = new ArrayList<>();
    private int ultraSolutionInitialSize = 0;
    // --- Superpairs state ---
    private final Map<Slot, ItemStack> superRewards = new HashMap<>();
    private Slot superSlot = null;
    // --- Shared state ---
    private boolean rememberPhase = true;
    private long lastClickTime = 0;

    public ExperimentSolver() {
        super(FrillsConfig.instance.solvers.experimentSolver.enabled);
        config = FrillsConfig.instance.solvers.experimentSolver;
    }

    public static ExperimentType getExperimentType() {
        if (Utils.isOnPrivateIsland() && mc.currentScreen instanceof GenericContainerScreen container) {
            String title = container.getTitle().getString();
            if (title.startsWith("Chronomatron (")) return ExperimentType.Chronomatron;
            if (title.startsWith("Ultrasequencer (")) return ExperimentType.Ultrasequencer;
            if (title.startsWith("Superpairs (")) return ExperimentType.Superpairs;
        }
        return ExperimentType.None;
    }

    private static boolean matchSuperStacks(ItemStack first, ItemStack second) {
        return first.getItem().equals(second.getItem())
                && first.getName().getString().equals(second.getName().getString())
                && first.getCount() == second.getCount()
                && Objects.equals(Utils.getTextureUrl(first), Utils.getTextureUrl(second));
    }

    private static boolean isDye(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof DyeItem
                || item.equals(Items.INK_SAC)
                || item.equals(Items.BONE_MEAL)
                || item.equals(Items.LAPIS_LAZULI)
                || item.equals(Items.COCOA_BEANS);
    }

    private static boolean isPowerup(ItemStack stack) {
        for (String line : Utils.getLoreLines(stack)) {
            if (Utils.toLower(line).contains("powerup")) {
                return true;
            }
        }
        return false;
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

    private void updatePhase(ItemStack stack) {
        Item item = stack.getItem();

        if (!rememberPhase && item.equals(Items.GLOWSTONE)) {
            rememberPhase = true;
            currentRoundProgress = 0;
        }

        if (rememberPhase && item.equals(Items.CLOCK)) {
            rememberPhase = false;
        }
    }

    @EventHandler
    private void onTick(WorldTickEvent event) {
        if (!isActive() || rememberPhase) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < config.clickDelay) return;

        ExperimentType type = getExperimentType();

        switch (type) {
            case Chronomatron:
                if (!config.chronomatron.enabled) break;
                if (!chronoSequence.isEmpty() && nextClickIndex < chronoSequence.size()) {
                    Slot slotToClick = chronoSequence.get(nextClickIndex);
                    Utils.clickSlot(slotToClick.id);
                    lastClickTime = currentTime;

                    LOGGER.info("[Chronomatron] Clicked slot ID: {} | Next index: {}/{}",
                            slotToClick.id, nextClickIndex + 1, chronoSequence.size());

                    nextClickIndex++;
                    // If configured, close the menu when we have reached the configured amount
                    // (user provides N meaning close after N-1 clicks)
                    try {
                        if (config.chronomatron.shouldClose) {
                            int threshold = Math.max(1, config.chronomatron.closeThreshold);
                            if (nextClickIndex >= threshold - 1) {
                                if (mc.player != null) mc.player.closeHandledScreen();
                            }
                        }
                    } catch (Throwable ignored) {
                    }
                }
                break;

            case Ultrasequencer:
                if (!config.ultrasequencer.enabled) break;
                if (!ultraSolution.isEmpty()) {
                    Slot slotToClick = ultraSolution.getFirst().slot;
                    Utils.clickSlot(slotToClick.id);
                    ultraSolution.removeFirst();
                    lastClickTime = currentTime;
                    // initialize initial size if not set
                    if (ultraSolutionInitialSize == 0) ultraSolutionInitialSize = Math.max(ultraSolution.size() + 1, 1);
                    int clicksDone = ultraSolutionInitialSize - ultraSolution.size();
                    // If configured, close the menu once we've clicked enough items
                    try {
                        if (config.ultrasequencer.shouldClose) {
                            int threshold = Math.max(1, config.ultrasequencer.closeThreshold);
                            if (clicksDone >= threshold - 1) {
                                if (mc.player != null) mc.player.closeHandledScreen();
                                ultraSolutionInitialSize = 0;
                            }
                        }
                    } catch (Throwable ignored) {
                    }
                    if (ultraSolution.isEmpty()) ultraSolutionInitialSize = 0;
                }
                break;

            case Superpairs:
                break;
        }
    }

    @EventHandler
    private void onSlotUpdate(SlotUpdateEvent event) {
        if (!isActive() || event.isInventory || event.slot == null) return;

        ExperimentType experimentType = getExperimentType();
        if (experimentType == ExperimentType.None) return;

        updatePhase(event.stack);

        switch (experimentType) {
            case Chronomatron:
                if (!config.chronomatron.enabled) break;
                if (!rememberPhase) break;

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
                break;

            case Ultrasequencer:
                if (!config.ultrasequencer.enabled) break;

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
                break;

            case Superpairs:
                if (!config.superpairs.enabled) break;
                if (isPowerup(event.stack)) {
                    SlotOptions.setBackground(event.slot, RenderColor.fromChroma(config.superpairs.powerupColor));
                    return;
                }
                if (Utils.isStainedGlass(event.stack) || Utils.isStainedGlassPane(event.stack) || event.stack.getItem().equals(Items.AIR)) {
                    break;
                }

                // Check if current slot matches the first slot of current pair
                if (superSlot != null && superSlot != event.slot) {
                    if (matchSuperStacks(event.stack, superSlot.getStack())) {
                        var color = RenderColor.fromChroma(config.superpairs.matchedColor);
                        SlotOptions.setBackground(event.slot, color);
                        SlotOptions.setBackground(superSlot, color);
                    }
                }
                // Check if current slot matches any previously seen card
                for (Map.Entry<Slot, ItemStack> solution : superRewards.entrySet()) {
                    if (!SlotOptions.hasBackground(event.slot) && !event.slot.equals(solution.getKey()) && matchSuperStacks(event.stack, solution.getValue())) {
                        var color = RenderColor.fromChroma(config.superpairs.matchingColor);
                        SlotOptions.setBackground(event.slot, color);
                        SlotOptions.setBackground(solution.getKey(), color);
                    }
                }
                // Track this click and set as current slot
                superRewards.put(event.slot, event.stack);
                superSlot = event.slot;
                SlotOptions.setSpoofed(event.slot, event.stack);
                break;
        }
    }

    @EventHandler
    private void onScreen(ScreenOpenEvent event) {
        if (isActive()) {
            rememberPhase = true;
            chronoSequence.clear();
            nextClickIndex = 0;
            currentRoundProgress = 0;

            ultraSolution.clear();
            ultraSolutionInitialSize = 0;

            superSlot = null;
            superRewards.clear();

            lastClickTime = System.currentTimeMillis();
        }
    }

    public enum ExperimentType {
        Chronomatron, Ultrasequencer, Superpairs, None
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
