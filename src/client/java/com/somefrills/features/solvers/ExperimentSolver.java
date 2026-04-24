package com.somefrills.features.solvers;

import com.somefrills.config.FrillsConfig;
import com.somefrills.config.solvers.SolverCategory.ExperimentSolverConfig;
import com.somefrills.features.core.Feature;
import com.somefrills.misc.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.somefrills.Main.mc;

/**
 * Fabric 1.21.10 port of the Kotlin AutoExperiments module.
 * Mirrors the exact behavior: tracks state transitions and only clicks when new items appear.
 * Uses AllConfig for dynamic settings.
 */
public class ExperimentSolver extends Feature {
    private final ExperimentSolverConfig config;

    private final Map<Integer, Integer> ultrasequencerOrder = new HashMap<>();
    private final List<Integer> chronomatronOrder = new ArrayList<>();
    private long lastClickTime = 0;
    private boolean hasAdded = false;
    private int lastAdded = 0;
    private int clicks = 0;

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

    private static boolean isValidChronoSlot(int idx) {
        return (11 <= idx && idx <= 19) || (30 <= idx && idx <= 38);
    }

    private static boolean isGlowstone(ItemStack stack) {
        return stack.getItem().equals(Items.GLOWSTONE);
    }

    private static boolean isClock(ItemStack stack) {
        return stack.getItem().equals(Items.CLOCK);
    }

    private static boolean isGlowstone(Slot s) {
        return isGlowstone(s.getStack());
    }

    private static boolean isClock(Slot s) {
        return isClock(s.getStack());
    }

    private static boolean isDye(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof DyeItem
                || item.equals(Items.INK_SAC)
                || item.equals(Items.BONE_MEAL)
                || item.equals(Items.LAPIS_LAZULI)
                || item.equals(Items.COCOA_BEANS);
    }

    private static boolean isDye(Slot s) {
        return isDye(s.getStack());
    }

    private static boolean isTerracotta(ItemStack stack) {
        return stack.getItem().toString().endsWith("terracotta");
    }

    private static boolean isTerracotta(Slot s) {
        return isTerracotta(s.getStack());
    }

    @Override
    protected void onEnable() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient client) {
        if (client == null || client.player == null) return;
        ClientPlayerEntity player = client.player;
        ScreenHandler handler = player.currentScreenHandler;
        if (handler == null) return;

        var type = getExperimentType();
        if (type == ExperimentType.None) {
            reset();
            return;
        }
        // Determine which experiment type
        if (config.chronomatron.enabled && type == ExperimentType.Chronomatron) {
            solveChronomatron(handler);
        } else if (config.ultrasequencer.enabled && type == ExperimentType.Ultrasequencer) {
            solveUltraSequencer(handler);
        }
    }

    private void solveChronomatron(ScreenHandler handler) {
        List<Slot> invSlots = handler.slots;
        int maxChronomatron = config.chronomatron.closeThreshold;

        // Check if slot 49 is glowstone AND last added slot is not enchanted (click registered)
        Slot slot49 = invSlots.get(49);
        Slot lastAddedSlot = invSlots.get(lastAdded);
        boolean slot49IsGlowstone = slot49.getStack() != null && isGlowstone(slot49);
        boolean lastAddedNotEnchanted = lastAddedSlot.getStack() != null && !isEnchanted(lastAddedSlot.getStack());

        if (slot49IsGlowstone && lastAddedNotEnchanted) {
            if (config.chronomatron.shouldClose && chronomatronOrder.size() > maxChronomatron) {
                ClientPlayerEntity player = MinecraftClient.getInstance().player;
                if (player != null) {
                    player.closeHandledScreen();
                }
            }
            hasAdded = false;
        }

        // Detect new item entering: slot 49 is clock
        if (!hasAdded && slot49.getStack() != null && isClock(slot49)) {
            for (int i = 11; i < 57; i++) { // Scan the colored glass/terracotta area (slots 11-56)
                if (!isValidChronoSlot(i)) continue;
                Slot s = invSlots.get(i);
                ItemStack stack = s.getStack();
                if (stack == null || stack.isEmpty()) continue;
                if (!isEnchanted(stack)) continue;
                if (!isTerracotta(stack)) continue;
                chronomatronOrder.add(i);
            }

            if (!chronomatronOrder.isEmpty()) {
                lastAdded = chronomatronOrder.getLast();
                hasAdded = true;
                clicks = 0;
            }
        }

        // Perform clicking: slot 49 is clock AND we have items to click
        if (hasAdded && slot49.getStack() != null && isClock(slot49)
                && chronomatronOrder.size() > clicks && System.currentTimeMillis() - lastClickTime > config.clickDelay) {
            int slotToClick = chronomatronOrder.get(clicks);
            sendClickPacket(handler, slotToClick);
            lastClickTime = System.currentTimeMillis();
            clicks++;
        }
    }

    private void solveUltraSequencer(ScreenHandler handler) {
        List<Slot> invSlots = handler.slots;
        int maxUltraSequencer = config.ultrasequencer.closeThreshold;

        // Reset when slot 49 becomes clock (new round)
        Slot slot49 = invSlots.get(49);
        if (slot49.getStack() != null && isClock(slot49)) {
            hasAdded = false;
        }

        // Detect and rebuild map when slot 49 becomes glowstone
        if (!hasAdded && slot49.getStack() != null && isGlowstone(slot49)) {
            ultrasequencerOrder.clear();

            for (int i = 0; i < invSlots.size(); i++) {
                Slot s = invSlots.get(i);
                if (s.getStack() != null && !s.getStack().isEmpty()) {
                    int stackSize = s.getStack().getCount();
                    if (isDye(s)) {
                        int idx = stackSize - 1;
                        ultrasequencerOrder.put(idx, i);
                    }
                }
            }

            hasAdded = true;
            clicks = 0;

            if (ultrasequencerOrder.size() > maxUltraSequencer && config.ultrasequencer.shouldClose) {
                ClientPlayerEntity player = MinecraftClient.getInstance().player;
                if (player != null) {
                    player.closeHandledScreen();
                }
            }
        }

        // Perform clicking: slot 49 is clock AND we have dyes to click
        if (slot49.getStack() != null && isClock(slot49)
                && ultrasequencerOrder.containsKey(clicks) && System.currentTimeMillis() - lastClickTime > config.clickDelay) {
            Integer slotToClick = ultrasequencerOrder.get(clicks);
            if (slotToClick != null) {
                sendClickPacket(handler, slotToClick);
            }
            lastClickTime = System.currentTimeMillis();
            clicks++;
        }
    }

    private void sendClickPacket(ScreenHandler handler, int slotIdx) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.interactionManager == null || client.player == null) {
            return;
        }

        client.interactionManager.clickSlot(
                handler.syncId,
                slotIdx,
                0,
                SlotActionType.PICKUP,
                client.player
        );
    }

    private boolean isEnchanted(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return stack.hasEnchantments() || stack.hasGlint();
    }

    private void reset() {
        ultrasequencerOrder.clear();
        chronomatronOrder.clear();
        hasAdded = false;
        lastAdded = 0;
        clicks = 0;
    }

    public enum ExperimentType {
        Chronomatron, Ultrasequencer, Superpairs, None
    }
}