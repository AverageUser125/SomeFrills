package com.glowplayer.features;

import com.glowplayer.utils.AllConfig;
import com.glowplayer.utils.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fabric 1.21.10 RNG Meter Display feature.
 * Automatically highlights and displays RNG meter items with enhanced tooltip information.
 * Reads lore from items in the RNG meter menu to show odds, runs needed, and progress.
 */
public class RNGMeterDisplay {
    private static final String RNG_METER_SUFFIX = "RNG Meter";
    private static final String ORIGINAL_LORE_NBT_KEY = "GlowPlayer_OriginalLore";
    // Pattern to match both single and dual percentage formats
    // Handles formatting codes like §8§m (dark gray + strikethrough), §o (italic), §l (bold), etc.
    // Matches: "Odds: TEXT (0.005%)" or "Odds: TEXT (§8§m0.005%§r 0.104%)"
    private static final Pattern ODDS_PATTERN = Pattern.compile("Odds: (.+?) \\(([§\\w.0-9]+)%([^)]*?)\\)");

    private boolean wasShiftPressed = false;
    // Keep track of original lore lines in memory to preserve styling
    private final java.util.Map<String, List<Text>> storedLoreMap = new java.util.HashMap<>();

    public RNGMeterDisplay() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient client) {
        if (client == null || client.player == null) return;

        if (!AllConfig.showRngMeter) return;

        ClientPlayerEntity player = client.player;
        ScreenHandler handler = player.currentScreenHandler;
        if (handler == null) return;

        String title = "";
        if (client.currentScreen != null) {
            Text txt = client.currentScreen.getTitle();
            if (txt != null) title = txt.getString();
        }

        // Check if we're in an RNG Meter menu
        if (!title.endsWith(RNG_METER_SUFFIX)) {
            return;
        }

        boolean shiftPressed = Utils.isShiftKeyPressed();

        // Scan for RNG meter items with valuable lore
        for (Slot slot : handler.slots) {
            ItemStack stack = slot.getStack();
            if (stack == null || stack.isEmpty()) continue;

            // Check if item has lore
            var loreComponent = stack.getComponents().get(DataComponentTypes.LORE);
            if (loreComponent == null) continue;
            List<Text> loreLines = loreComponent.lines();
            if (loreLines == null || loreLines.isEmpty()) continue;

            String loreText = loreLines
                    .stream()
                    .map(Text::getString)
                    .reduce((a, b) -> a + "\n" + b)
                    .orElse("");

            // Check if this item contains RNG odds information
            if (loreText.contains("Odds:")) {
                if (shiftPressed) {
                    // Shift is pressed - convert to fractions
                    analyzeRngItem(stack, loreText, loreLines);
                } else if (wasShiftPressed) {
                    // Shift was released - restore original lore
                    restoreOriginalLore(stack);
                }
            }
        }

        wasShiftPressed = shiftPressed;
    }

    /**
     * Analyzes an RNG meter item and extracts relevant information from lore.
     * Stores original lore before modifying, so it can be restored later.
     *
     * @param stack     The item stack to analyze
     * @param loreText  The full lore text from the item
     * @param loreLines The original lore lines
     */
    private void analyzeRngItem(ItemStack stack, String loreText, List<Text> loreLines) {
        // Store original lore if not already stored
        if (!hasStoredOriginalLore(stack)) {
            storeOriginalLore(stack, loreLines);
            debug("Stored original lore for item: " + stack.getName().getString());
        }

        // Parse odds pattern
        Matcher oddsMatcher = ODDS_PATTERN.matcher(loreText);

        debug("===== ANALYZING ITEM =====");
        debug("Item: " + stack.getName().getString());
        debug("Checking ODDS_PATTERN...");

        if (oddsMatcher.find()) {
            debug("✓ ODDS_PATTERN matched!");
            String fullMatch = oddsMatcher.group(0);  // Full: "Odds: Text (5%)"
            String percentage1 = Utils.cleanFormatting(oddsMatcher.group(2)); // Strip formatting codes
            String percentage2 = null;

            String remaining = oddsMatcher.group(3); // The rest after first percentage
            if (remaining != null && !remaining.trim().isEmpty()) {
                // Extract second percentage from remaining text
                Matcher percent2Matcher = Pattern.compile("([0-9.]+)%").matcher(remaining);
                if (percent2Matcher.find()) {
                    percentage2 = percent2Matcher.group(1);
                }
            }

            // Create the fraction replacement text with preserved formatting
            Text replacementText = createOddsFractionText(fullMatch, percentage1, percentage2);

            // Replace the Odds line in the lore using utility function
            boolean replaced = Utils.replaceLoreLine(stack, "Odds:", replacementText);
            if (replaced) {
                debug("SUCCESS - Replaced Odds line");
            } else {
                debug("FAILED - Could not find Odds line to replace");
            }
        } else {
            debug("✗ ODDS_PATTERN did not match");
        }
    }

    /**
     * Creates a Text object with odds displayed as a fraction only.
     * Preserves the original formatting and text between Odds and percentage.
     */
    private Text createOddsFractionText(String fullOddsMatch, String percentage1, String percentage2) {
        int chance1 = calculateChance(percentage1);
        String frac1 = Utils.formatNumberWithCommas(chance1);

        // Extract everything between "Odds: " and the opening parenthesis
        // This preserves all formatting codes and text in the middle
        Pattern beforePercentPattern = Pattern.compile("Odds: (.+?) \\(");
        Matcher matcher = beforePercentPattern.matcher(fullOddsMatch);
        String beforeParen = "";
        if (matcher.find()) {
            beforeParen = matcher.group(1);
        }

        if (percentage2 != null) {
            // Dual format: show strikethrough first, then second
            int chance2 = calculateChance(percentage2);
            String frac2 = Utils.formatNumberWithCommas(chance2);
            // Create the replacement with proper formatting
            String replacement = "Odds: " + beforeParen + " (§8§m1/" + frac1 + "§r §71/" + frac2 + ")";
            return Text.literal(replacement);
        } else {
            // Single format - preserve original text exactly as it was
            String replacement = "Odds: " + beforeParen + " (1/" + frac1 + ")";
            return Text.literal(replacement);
        }
    }

    /**
     * Stores the original lore component in memory.
     * This preserves all styling and formatting codes without any loss.
     */
    private void storeOriginalLore(ItemStack stack, List<Text> loreLines) {
        String itemKey = getItemKey(stack);
        // Create a copy of the lore lines to store in memory
        storedLoreMap.put(itemKey, new ArrayList<>(loreLines));

        // Also mark in NBT so we can detect if lore was modified
        var customDataComponent = stack.getComponents().get(DataComponentTypes.CUSTOM_DATA);
        NbtCompound nbt;

        if (customDataComponent != null) {
            nbt = customDataComponent.copyNbt();
        } else {
            nbt = new NbtCompound();
        }

        nbt.putBoolean(ORIGINAL_LORE_NBT_KEY, true);
        stack.set(DataComponentTypes.CUSTOM_DATA, net.minecraft.component.type.NbtComponent.of(nbt));
    }

    /**
     * Gets a unique key for an item based on its name and NBT hash.
     */
    private String getItemKey(ItemStack stack) {
        return stack.getName().getString() + "_" + Integer.toHexString(stack.hashCode());
    }

    /**
     * Checks if original lore has been stored.
     */
    private boolean hasStoredOriginalLore(ItemStack stack) {
        NbtCompound customData = Utils.getCustomData(stack);
        return customData != null && customData.contains(ORIGINAL_LORE_NBT_KEY);
    }

    /**
     * Restores the original lore from the in-memory storage.
     * This restores the exact Text objects with all their styling intact.
     */
    private void restoreOriginalLore(ItemStack stack) {
        String itemKey = getItemKey(stack);
        List<Text> originalLore = storedLoreMap.get(itemKey);

        if (originalLore == null) {
            return;
        }

        // Restore the original lore component directly
        stack.set(DataComponentTypes.LORE, new LoreComponent(originalLore));
        debug("Restored original lore for item: " + stack.getName().getString());

        // Clean up the stored NBT data
        var customDataComponent = stack.getComponents().get(DataComponentTypes.CUSTOM_DATA);
        if (customDataComponent != null) {
            NbtCompound nbt = customDataComponent.copyNbt();
            nbt.remove(ORIGINAL_LORE_NBT_KEY);
            if (!nbt.isEmpty()) {
                stack.set(DataComponentTypes.CUSTOM_DATA, net.minecraft.component.type.NbtComponent.of(nbt));
            } else {
                stack.remove(DataComponentTypes.CUSTOM_DATA);
            }
        }

        // Clean up memory
        storedLoreMap.remove(itemKey);
    }

    /**
     * Calculates the denominator for a fraction from a percentage.
     * E.g., "5%" -> 20 (representing 1/20)
     */
    private int calculateChance(String percentage) {
        try {
            double percent = Double.parseDouble(percentage);
            if (percent > 0) {
                return (int) (100.0 / percent);
            }
        } catch (NumberFormatException e) {
            // Invalid percentage
        }
        return 0;
    }

    /**
     * Debug print utility - only prints if AllConfig.debugRngDisplay is enabled.
     */
    private void debug(String message) {
        if (AllConfig.debugRngDisplay) {
            System.out.println("[RNGMeter] " + message);
        }
    }
}




