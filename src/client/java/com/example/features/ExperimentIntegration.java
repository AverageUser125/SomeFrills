package com.example.features;

import com.example.utils.AllConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.example.Main.mc;

/**
 * Fabric 1.21.10 port of the Kotlin AutoExperiments module.
 * Mirrors the exact behavior: tracks state transitions and only clicks when new items appear.
 * Uses AllConfig for dynamic settings.
 */
public class ExperimentIntegration {
    // Regex pattern for experiment table detection
    // Matches: Superpairs, Chronomatron, Ultrasequencer with optional suffixes like (Metaphysical), ➜ Stakes, Rewards
    // Also matches: Experimentation Table, Experiment Over
    private static final Pattern EXPERIMENT_PATTERN = Pattern.compile(
            "(?:Superpairs|Chronomatron|Ultrasequencer) ?(?:\\(.+\\)|➜ Stakes|Rewards)|Experiment(?:ation Tabl| [Oo]v)er?",
            Pattern.CASE_INSENSITIVE
    );
    private final Map<Integer, Integer> ultrasequencerOrder = new HashMap<>();
    private final List<Integer> chronomatronOrder = new ArrayList<>();
    private long lastClickTime = 0;
    private boolean hasAdded = false;
    private int lastAdded = 0;
    private int clicks = 0;

    public ExperimentIntegration() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient client) {
        if (client == null || client.player == null) return;
        ClientPlayerEntity player = client.player;
        ScreenHandler handler = player.currentScreenHandler;
        if (handler == null) return;

        String title = "";
        if (client.currentScreen != null) {
            Text txt = client.currentScreen.getTitle();
            if (txt != null) title = txt.getString();
        }

        // Use regex pattern to detect experiment table
        if (EXPERIMENT_PATTERN.matcher(title).find()) {

            // Determine which experiment type
            if (AllConfig.enableChronomatron && title.contains("Chronomatron")) {
                solveChronomatron(handler);
            } else if (AllConfig.enableUltrasequencer && title.contains("Ultrasequencer")) {
                solveUltraSequencer(handler);
            } else if (title.contains("Superpairs")) {
            }
        } else {
            if (!title.isEmpty()) {
                reset();
            }
        }
    }

    private void solveChronomatron(ScreenHandler handler) {
        List<Slot> invSlots = handler.slots;
        int maxChronomatron = AllConfig.getMaxXp ? 15 : (11 - AllConfig.serumCount);

        // Check if slot 49 is glowstone AND last added slot is not enchanted (click registered)
        Slot slot49 = invSlots.get(49);
        Slot lastAddedSlot = invSlots.get(lastAdded);
        boolean slot49IsGlowstone = slot49.getStack() != null && isItem(slot49.getStack(), "minecraft:glowstone");
        boolean lastAddedNotEnchanted = lastAddedSlot.getStack() != null && !isEnchanted(lastAddedSlot.getStack());

        if (slot49IsGlowstone && lastAddedNotEnchanted) {
            if (AllConfig.autoClose && chronomatronOrder.size() > maxChronomatron) {
                if (mc.player != null) {
                    mc.player.closeHandledScreen();
                }
            }
            hasAdded = false;
        }

        // Detect new item entering: slot 49 is clock
        if (!hasAdded && slot49.getStack() != null && isItem(slot49.getStack(), "minecraft:clock")) {
            // Scan for enchanted TERRACOTTA ONLY, filtering duplicates
            // Each color appears in 2 rows (row height = 9), so skip slots that are 9 apart (same color)

            int lastSlotAdded = -1;
            for (int i = 11; i < 57; i++) { // Scan the colored glass/terracotta area (slots 11-56)
                Slot s = invSlots.get(i);
                if (s.getStack() != null && !s.getStack().isEmpty() && isEnchanted(s.getStack())) {
                    String itemName = Registries.ITEM.getId(s.getStack().getItem()).toString();

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
                lastAdded = chronomatronOrder.get(chronomatronOrder.size() - 1);
                hasAdded = true;
                clicks = 0;
            }
        }

        // Perform clicking: slot 49 is clock AND we have items to click
        if (hasAdded && slot49.getStack() != null && isItem(slot49.getStack(), "minecraft:clock")
                && chronomatronOrder.size() > clicks && System.currentTimeMillis() - lastClickTime > AllConfig.clickDelay) {
            int slotToClick = chronomatronOrder.get(clicks);
            sendClickPacket(handler, slotToClick);
            lastClickTime = System.currentTimeMillis();
            clicks++;
        }
    }

    private void solveUltraSequencer(ScreenHandler handler) {
        List<Slot> invSlots = handler.slots;
        int maxUltraSequencer = AllConfig.getMaxXp ? 20 : (9 - AllConfig.serumCount);

        // Reset when slot 49 becomes clock (new round)
        Slot slot49 = invSlots.get(49);
        if (slot49.getStack() != null && isItem(slot49.getStack(), "minecraft:clock")) {
            hasAdded = false;
        }

        // Detect and rebuild map when slot 49 becomes glowstone
        if (!hasAdded && slot49.getStack() != null && isItem(slot49.getStack(), "minecraft:glowstone")) {
            ultrasequencerOrder.clear();

            for (int i = 0; i < invSlots.size(); i++) {
                Slot s = invSlots.get(i);
                if (s.getStack() != null && !s.getStack().isEmpty()) {
                    int stackSize = s.getStack().getCount();
                    boolean isDye = isItem(s.getStack(), "minecraft:dye");

                    if (isDye) {
                        int idx = stackSize - 1;
                        ultrasequencerOrder.put(idx, i);
                    }
                }
            }

            hasAdded = true;
            clicks = 0;

            if (ultrasequencerOrder.size() > maxUltraSequencer && AllConfig.autoClose) {
                if (mc.player != null) {
                    mc.player.closeHandledScreen();
                }
            }
        }

        // Perform clicking: slot 49 is clock AND we have dyes to click
        if (slot49.getStack() != null && isItem(slot49.getStack(), "minecraft:clock")
                && ultrasequencerOrder.containsKey(clicks) && System.currentTimeMillis() - lastClickTime > AllConfig.clickDelay) {
            Integer slotToClick = ultrasequencerOrder.get(clicks);
            if (slotToClick != null) {
                sendClickPacket(handler, slotToClick);
            }
            lastClickTime = System.currentTimeMillis();
            clicks++;
        }
    }

    private void sendClickPacket(ScreenHandler handler, int slotIdx) {
        if (mc.interactionManager == null || mc.player == null) {
            return;
        }

        mc.interactionManager.clickSlot(
                handler.syncId,
                slotIdx,
                0,
                SlotActionType.PICKUP,
                mc.player
        );
    }

    private boolean isItem(ItemStack stack, String itemId) {
        if (stack == null || stack.isEmpty()) return false;
        String actualId = Registries.ITEM.getId(stack.getItem()).toString();

        // Handle dye variants for 1.21.10 (white_dye, red_dye, etc.) and dye-like items
        if (itemId.equals("minecraft:dye")) {
            return actualId.endsWith("_dye") ||
                    actualId.equals("minecraft:bone_meal") ||
                    actualId.equals("minecraft:lapis_lazuli") ||
                    actualId.equals("minecraft:cocoa_beans") ||
                    actualId.equals("minecraft:ink_sac") ||
                    actualId.equals("minecraft:glow_ink_sac");
        }

        return actualId.equals(itemId);
    }

    private boolean isEnchanted(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        // Check for both actual enchantments AND the enchanted glint effect
        return stack.hasEnchantments() || stack.hasGlint();
    }

    private void reset() {
        ultrasequencerOrder.clear();
        chronomatronOrder.clear();
        hasAdded = false;
        lastAdded = 0;
        clicks = 0;
    }
}
