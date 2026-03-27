package com.somefrills.features.solvers;

import com.somefrills.Main;
import com.somefrills.config.*;
import com.somefrills.events.ScreenOpenEvent;
import com.somefrills.events.ScreenRenderEvent;
import com.somefrills.events.WorldTickEvent;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.somefrills.Main.mc;

public class ExperimentSolver {
    public static final Feature instance = new Feature("experimentSolver");

    @SettingDescription("Enable automatic solving of Chronomatron experiments")
    public static final SettingBool chronomatron = new SettingBool(true);

    @SettingDescription("Enable automatic solving of Ultrasequencer experiments")
    public static final SettingBool ultrasequencer = new SettingBool(true);

    @SettingDescription("Try to maximize XP when solving experiments")
    public static final SettingBool getMaxXp = new SettingBool(false);

    @SettingDescription("Automatically close the experiment screen when conditions are met")
    public static final SettingBool autoClose = new SettingBool(true);

    @SettingDescription("Number of serum items to keep in Chronomatron")
    public static final SettingIntSlider serumCount = new SettingIntSlider(3, 0, 3);

    @SettingDescription("Delay between automated clicks (ms)")
    public static final SettingInt clickDelay = new SettingInt(100);

    private static final Map<Integer, Integer> ultrasequencerOrder = new HashMap<>();
    private static final List<Integer> chronomatronOrder = new ArrayList<>();
    private static long lastClickTime = 0;
    private static boolean hasAdded = false;
    private static int lastAdded = 0;
    private static int clicks = 0;

    @EventHandler
    public static void onHudTick(WorldTickEvent event) {
        onHudTick();
    }

    public static void onHudTick() {
        if (mc == null || mc.player == null) return;
        ClientPlayerEntity player = mc.player;
        ScreenHandler handler = player.currentScreenHandler;
        if (handler == null) return;

        // Use the helper to determine the current experiment type (if any)
        ExperimentType type = getExperimentType();
        switch (type) {
            case Chronomatron -> { if (chronomatron.value()) solveChronomatron(handler); else reset(); }
            case Ultrasequencer -> { if (ultrasequencer.value()) solveUltraSequencer(handler); else reset(); }
            case Superpairs -> reset(); // Not implemented yet — ensure state is reset
            case None -> reset(); // If we're no longer on an experiment screen, clear state
        }
    }

    private static void solveChronomatron(ScreenHandler handler) {
        List<Slot> invSlots = handler.slots;
        int maxChronomatron = getMaxXp.value() ? 15 : (11 - serumCount.value());

        // Check if slot 49 is glowstone AND last added slot is not enchanted (click registered)
        ItemStack stack49 = invSlots.get(49).getStack();
        Slot lastAddedSlot = invSlots.get(lastAdded);
        boolean slot49IsGlowstone = isItem(stack49, "minecraft:glowstone");
        boolean lastAddedNotEnchanted = lastAddedSlot.getStack() != null && !isEnchanted(lastAddedSlot.getStack());

        if (slot49IsGlowstone && lastAddedNotEnchanted) {
            if (autoClose.value() && chronomatronOrder.size() > maxChronomatron) {
                if (mc.player != null) {
                    mc.player.closeScreen();
                }
            }
            hasAdded = false;
        }

        // Detect new item entering: slot 49 is clock
        if (!hasAdded && isItem(stack49, "minecraft:clock")) {
            // Scan for enchanted TERRACOTTA ONLY, filtering duplicates
            // Each color appears in 2 rows (row height = 9), so skip slots that are 9 apart (same color)

            int lastSlotAdded = -1;
            for (int i = 11; i < 57; i++) { // Scan the colored glass/terracotta area (slots 11-56)
                Slot s = invSlots.get(i);
                if (s.getStack() != null && !s.getStack().isEmpty() && isEnchanted(s.getStack())) {
                    String itemName = Registries.ITEM.getKey(s.getStack().getItem()).toString();

                    // ONLY add terracotta blocks, skip everything else
                    if (!itemName.contains("terracotta")) {
                        continue;
                    }

                    // Skip if this is a duplicate (same color, 9 slots away)
                    if (lastSlotAdded != -1 && (i - lastSlotAdded) == 9) {
                        continue;
                    }

                    chronomatronOrder.add(i);
                    lastSlotAdded = i;
                }
            }

            if (!chronomatronOrder.isEmpty()) {
                lastAdded = chronomatronOrder.getLast();
                hasAdded = true;
                clicks = 0;
            }
        }

        // Perform clicking: slot 49 is clock AND we have items to click
        if (hasAdded && isItem(stack49, "minecraft:clock")
                && chronomatronOrder.size() > clicks && System.currentTimeMillis() - lastClickTime > clickDelay.value()) {
            int slotToClick = chronomatronOrder.get(clicks);
            Utils.clickSlot(slotToClick);
            lastClickTime = System.currentTimeMillis();
            clicks++;
        }
    }

    private static void solveUltraSequencer(ScreenHandler handler) {
        List<Slot> invSlots = handler.slots;
        int maxUltraSequencer = getMaxXp.value() ? 20 : (9 - serumCount.value());

        ItemStack stack49 = invSlots.get(49).getStack();

        // Remember the mapping when glowstone appears
        if (!hasAdded && isItem(stack49, "minecraft:glowstone")) {
            ultrasequencerOrder.clear();
            for (int i = 0; i < invSlots.size(); i++) {
                ItemStack stack = invSlots.get(i).getStack();
                if (stack != null && !stack.isEmpty() && isDye(stack)) {
                    ultrasequencerOrder.put(stack.getCount() - 1, i); // stackSize-1 -> slot
                }
            }
            hasAdded = true; // mapping remembered
            clicks = 0;

            if (ultrasequencerOrder.size() > maxUltraSequencer && autoClose.value() && mc.player != null) {
                mc.player.closeScreen();
            }
        }

        // Perform clicking only when slot 49 is clock
        if (isItem(stack49, "minecraft:clock") && hasAdded
                && clicks < ultrasequencerOrder.size()
                && System.currentTimeMillis() - lastClickTime > clickDelay.value()) {

            Integer slotToClick = ultrasequencerOrder.get(clicks);
            if (slotToClick != null) {
                Utils.clickSlot(slotToClick);
            }
            lastClickTime = System.currentTimeMillis();
            clicks++;
        }

        // Reset at the start of a new round if necessary
        if (isItem(stack49, "minecraft:clock") && clicks >= ultrasequencerOrder.size()) {
            hasAdded = false;
            clicks = 0;
            ultrasequencerOrder.clear();
        }
    }

    private static boolean isItem(ItemStack stack, String itemId) {
        if (stack == null || stack.isEmpty()) return false;
        String id = Registries.ITEM.getKey(stack.getItem()).toString();
        return id.equals(itemId);
    }

    private static boolean isDye(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        String id = Registries.ITEM.getKey(stack.getItem()).toString();
        return id.contains("dye")
                || id.contains("ink_sac") || id.contains("lapis")
                || id.contains("rose") || id.contains("cactus")
                || id.contains("cocoa")
                || id.contains("bone_meal");
    }

    private static boolean isEnchanted(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        // Check for both actual enchantments AND the enchanted glint effect
        return stack.hasEnchantments() || stack.hasGlint();
    }

    private static void reset() {
        ultrasequencerOrder.clear();
        chronomatronOrder.clear();
        hasAdded = false;
        lastAdded = 0;
        clicks = 0;
    }
    public enum ExperimentType {
        Chronomatron,
        Ultrasequencer,
        Superpairs,
        None
    }

    public static ExperimentType getExperimentType() {
        if (mc == null || mc.currentScreen == null) return ExperimentType.None;
        Text titleText = mc.currentScreen.getTitle();
        if (titleText == null) return ExperimentType.None;

        String title = titleText.getString();
        if (title == null || title.isEmpty()) return ExperimentType.None;

        Main.LOGGER.info("Experiment screen detected with title: {}", title);
        String lower = title.toLowerCase();
        if (lower.contains("chronomatron")) return ExperimentType.Chronomatron;
        if (lower.contains("ultrasequencer")) return ExperimentType.Ultrasequencer;
        if (lower.contains("superpairs")) return ExperimentType.Superpairs;

        return ExperimentType.None;
    }
}