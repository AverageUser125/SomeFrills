package com.example;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

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
    // Pattern to match both single and dual percentage formats
    // Handles formatting codes like §8§m (dark gray + strikethrough), §o (italic), §l (bold), etc.
    // Matches: "Odds: TEXT (0.005%)" or "Odds: TEXT (§8§m0.005%§r 0.104%)"
    private static final Pattern ODDS_PATTERN = Pattern.compile("Odds: (.+?) \\(([§\\w.0-9]+)%([^)]*?)\\)");
    private static final Pattern RUNS_PATTERN = Pattern.compile("(Dungeon Score|Slayer XP): ([0-9,]+)/([0-9,]+)");
    private static final Pattern PROGRESS_PATTERN = Pattern.compile("Progress: ([0-9.]+)%\\s+([0-9,]+)/([0-9,.a-z]+)");

    private boolean showFractions = false;
    private boolean pressedShiftLast = false;

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
                analyzeRngItem(stack, loreText);
            }
        }

    }

    /**
     * Analyzes an RNG meter item and extracts relevant information from lore.
     * Looks for odds, chance percentages, and progress information.
     * Sets custom tooltips with formatted RNG data.
     * This is called every tick to keep tooltips up-to-date.
     *
     * @param stack    The item stack to analyze
     * @param loreText The full lore text from the item
     */
    private void analyzeRngItem(ItemStack stack, String loreText) {
        List<Text> tooltipLines = new ArrayList<>();

        // Handle shift key toggle for fraction display
        boolean shiftPressed = isShiftKeyPressed();
        if (!pressedShiftLast && shiftPressed) {
            showFractions = !showFractions;
            debug("SHIFT PRESSED - Toggled to: " + (showFractions ? "FRACTIONS" : "PERCENTAGES"));
            // Force update by removing marker so tooltip gets reapplied immediately
            loreText = loreText.replace("§6§lRNG Info:", "REMOVED_MARKER");
        }
        pressedShiftLast = shiftPressed;

        // Skip if we've already applied our tooltip to this item
        if (loreText.contains("§6§lRNG Info:")) {
            return;
        }

        debug("===== ANALYZING ITEM =====");
        debug("Item: " + stack.getName().getString());
        debug("Lore text length: " + loreText.length());
        debug("ACTUAL LORE:\n" + loreText);

        // Parse odds pattern
        Matcher oddsMatcher = ODDS_PATTERN.matcher(loreText);

        debug("Checking ODDS_PATTERN...");
        if (oddsMatcher.find()) {
            debug("✓ ODDS_PATTERN matched!");
            String odds = oddsMatcher.group(1);
            String percentage1 = cleanFormatting(oddsMatcher.group(2)); // Strip formatting codes
            String percentage2 = null;

            String remaining = oddsMatcher.group(3); // The rest after first percentage
            if (remaining != null && !remaining.trim().isEmpty()) {
                // Extract second percentage from remaining text
                Matcher percent2Matcher = Pattern.compile("([0-9.]+)%").matcher(remaining);
                if (percent2Matcher.find()) {
                    percentage2 = percent2Matcher.group(1);
                }
            }

            addOddsTooltip(tooltipLines, odds, percentage1, percentage2);
        } else {
            debug("✗ ODDS_PATTERN did not match");
        }

        // Parse progress pattern - try both formats
        Matcher runsMatcher = RUNS_PATTERN.matcher(loreText);
        Matcher progressMatcher = PROGRESS_PATTERN.matcher(loreText);

        debug("Checking RUNS_PATTERN...");
        if (runsMatcher.find()) {
            debug("✓ RUNS_PATTERN matched!");
            String type = runsMatcher.group(1);
            String having = runsMatcher.group(2);
            String needed = runsMatcher.group(3);
            addProgressTooltip(tooltipLines, type, having, needed);
        } else {
            debug("✗ RUNS_PATTERN did not match");
            debug("Checking PROGRESS_PATTERN...");
            if (progressMatcher.find()) {
                debug("✓ PROGRESS_PATTERN matched!");
                String progressPercent = progressMatcher.group(1);
                String having = progressMatcher.group(2);
                String needed = progressMatcher.group(3);
                addProgressTooltip(tooltipLines, progressPercent, having, needed);
            } else {
                debug("✗ PROGRESS_PATTERN did not match");
            }
        }

        debug("Tooltip lines collected: " + tooltipLines.size());

        // Set the tooltip if we found any RNG information
        if (!tooltipLines.isEmpty()) {
            debug("ATTEMPTING to set tooltip with " + tooltipLines.size() + " lines");

            try {
                // Get the existing lore component
                var loreComponent = stack.getComponents().get(DataComponentTypes.LORE);
                List<Text> allLines = new ArrayList<>();

                if (loreComponent != null) {
                    // Start with existing lore
                    allLines.addAll(loreComponent.lines());
                    debug("Added " + loreComponent.lines().size() + " existing lore lines");
                }

                // Add a separator and our RNG info
                allLines.add(Text.literal(""));
                allLines.addAll(tooltipLines);

                debug("Total lines before creating component: " + allLines.size());

                // Create new lore component
                LoreComponent newLoreComponent = new LoreComponent(allLines);
                debug("LoreComponent created successfully");

                // Attempt to set it
                stack.set(DataComponentTypes.LORE, newLoreComponent);
                debug("stack.set() called");
                debug("SUCCESS - Set tooltip with " + tooltipLines.size() + " info lines");
            } catch (Exception e) {
                // Log the full exception with stack trace
                System.err.println("[RNGMeter] FAILED to set tooltip on ItemStack");
                System.err.println("[RNGMeter] Exception: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    /**
     * Adds odds information to tooltip, with support for fraction display toggle.
     * Handles both single percentage "Odds: TEXT (5%)" and dual percentage "Odds: TEXT (5% 10%)" formats.
     */
    private void addOddsTooltip(List<Text> tooltipLines, String odds, String percentage1, String percentage2) {
        tooltipLines.add(Text.literal("§6§lRNG Info:"));

        if (showFractions) {
            // Show as fractions
            int chance1 = calculateChance(percentage1);
            String frac1 = formatNumber(chance1);

            if (percentage2 != null) {
                // Dual format: show strikethrough first, then second
                int chance2 = calculateChance(percentage2);
                String frac2 = formatNumber(chance2);
                tooltipLines.add(Text.literal("§7Odds: §b" + odds + " §7(§8§m1/" + frac1 + "§r §71/" + frac2 + ")"));
            } else {
                // Single format
                tooltipLines.add(Text.literal("§7Odds: §b" + odds + " §7(§b1/" + frac1 + ")"));
            }
        } else {
            // Show as percentages
            if (percentage2 != null) {
                // Dual format: show strikethrough first, then second
                tooltipLines.add(Text.literal("§7Odds: §b" + odds + " §7(§8§m" + percentage1 + "%§r §7" + percentage2 + "%§7)"));
            } else {
                // Single format
                tooltipLines.add(Text.literal("§7Odds: §b" + odds + " §7(§e" + percentage1 + "%§7)"));
            }
        }
    }

    /**
     * Adds progress information to tooltip with runs/bosses needed calculation.
     * Can handle both "Dungeon Score: X/Y" format and "Progress: X%" format.
     * Both display the same way - just showing the amounts.
     */
    private void addProgressTooltip(List<Text> tooltipLines, String typeOrPercent, String having, String needed) {
        tooltipLines.add(Text.literal("§6§lProgress:"));

        // Display the same way regardless of format - just show the amount as XP
        tooltipLines.add(Text.literal("§7XP: §d" + having + "§7/§d" + needed));

        try {
            int havingVal = parseInt(having);
            int neededVal = parseInt(needed);
            if (havingVal >= 0 && neededVal > 0) {
                int remaining = neededVal - havingVal;
                tooltipLines.add(Text.literal("§7Remaining: §c" + formatNumber(remaining)));
            }
        } catch (NumberFormatException e) {
            // Could not parse numbers
        }
    }

    /**
     * Checks if shift key is currently pressed using GLFW.
     */
    private boolean isShiftKeyPressed() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.getWindow() == null) return false;

            long window = client.getWindow().getHandle();
            int shiftLeft = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT);
            int shiftRight = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT);

            return shiftLeft == GLFW.GLFW_PRESS || shiftRight == GLFW.GLFW_PRESS;
        } catch (Exception e) {
            if (AllConfig.debugRngDisplay) {
                System.err.println("[RNGMeter] FAILED - Error checking shift key: " + e.getMessage());
            }
            return false;
        }
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
     * Formats a number with thousand separators.
     * E.g., 1234 -> "1,234"
     */
    private String formatNumber(int number) {
        return String.format("%,d", number);
    }

    /**
     * Safely parses a number string that may contain commas and/or 'k' suffix.
     * E.g., "1,234" -> 1234, "918.6k" -> 918600, "1.5m" -> 1500000
     */
    private int parseInt(String value) {
        // Remove commas first
        value = value.replace(",", "").toLowerCase().trim();

        // Handle 'k' suffix (thousands)
        if (value.endsWith("k")) {
            double num = Double.parseDouble(value.substring(0, value.length() - 1));
            return (int) (num * 1000);
        }

        // Handle 'm' suffix (millions)
        if (value.endsWith("m")) {
            double num = Double.parseDouble(value.substring(0, value.length() - 1));
            return (int) (num * 1000000);
        }

        // Regular number
        return Integer.parseInt(value);
    }

    /**
     * Debug print utility - only prints if AllConfig.debugRngDisplay is enabled.
     */
    private void debug(String message) {
        if (AllConfig.debugRngDisplay) {
            System.out.println("[RNGMeter] " + message);
        }
    }

    /**
     * Removes Minecraft formatting codes (§X) from a string.
     * E.g., "§8§m0.005§r" -> "0.005"
     * Matches any character after § (color codes, modifiers, reset, etc.)
     */
    private String cleanFormatting(String text) {
        return text.replaceAll("§.", "");
    }
}





