package com.glowplayer.utils;

import com.google.common.base.Splitter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.glowplayer.Main.mc;


public class Utils {
    public static final MessageIndicator glowPlayerIndicator = new MessageIndicator(0x5ca0bf, null, Text.of("Message from GlowPlayer mod."), "GlowPlayer Mod");

    public static String toPlain(Text text) {
        if (text != null) {
            return Formatting.strip(text.getString());
        }
        return "";
    }

    public static ItemStack getHeldItem() {
        return mc.player != null ? mc.player.getMainHandStack() : ItemStack.EMPTY;
    }

    private static List<Text> getLoreText(ItemStack stack) {
        LoreComponent lore = stack.getComponents().get(DataComponentTypes.LORE);
        if (lore != null) {
            return lore.lines();
        }
        return new ArrayList<>();
    }

    public static List<String> getLoreLines(ItemStack stack) {
        List<String> lines = new ArrayList<>();
        for (Text line : getLoreText(stack)) {
            lines.add(toPlain(line).trim());
        }
        return lines;
    }

    /**
     * Sets the lore lines on an ItemStack.
     *
     * @param stack The ItemStack to modify
     * @param lines List of Text objects to set as lore
     */
    public static void setLoreLines(ItemStack stack, List<Text> lines) {
        if (stack != null && !stack.isEmpty()) {
            LoreComponent loreComponent = new LoreComponent(lines);
            stack.set(DataComponentTypes.LORE, loreComponent);
        }
    }

    /**
     * Sets the lore on an ItemStack from plain text strings.
     *
     * @param stack The ItemStack to modify
     * @param lines List of plain text strings to set as lore
     */
    public static void setLoreText(ItemStack stack, List<String> lines) {
        if (stack != null && !stack.isEmpty()) {
            List<Text> textLines = new ArrayList<>();
            for (String line : lines) {
                textLines.add(Text.literal(line));
            }
            setLoreLines(stack, textLines);
        }
    }

    /**
     * Replaces a specific lore line that contains a search string with a replacement Text.
     * The replacement text should include its own formatting codes (§X).
     *
     * @param stack           The ItemStack to modify
     * @param searchString    The string to search for in lore lines
     * @param replacementText The Text object to replace with
     * @return true if the line was found and replaced, false otherwise
     */
    public static boolean replaceLoreLine(ItemStack stack, String searchString, Text replacementText) {
        if (stack == null || stack.isEmpty()) return false;

        LoreComponent loreComponent = stack.getComponents().get(DataComponentTypes.LORE);
        if (loreComponent == null) return false;

        List<Text> allLines = new ArrayList<>(loreComponent.lines());
        boolean found = false;

        for (int i = 0; i < allLines.size(); i++) {
            Text currentLine = allLines.get(i);
            if (currentLine.getString().contains(searchString)) {
                // Just replace with the replacement text as-is
                // The formatting codes in the text will handle colors
                allLines.set(i, replacementText);
                found = true;
                break;
            }
        }

        if (found) {
            setLoreLines(stack, allLines);
        }

        return found;
    }

    public static String getRightClickAbility(ItemStack stack) {
        for (String line : getLoreLines(stack)) {
            if (line.contains("Ability: ") && line.endsWith("RIGHT CLICK")) {
                return line;
            }
        }
        return "";
    }

    public static NbtCompound getCustomData(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
            if (data != null) {
                return data.copyNbt();
            }
        }
        return null;
    }

    public static String getSkyblockId(NbtCompound customData) {
        if (customData != null && customData.contains("id")) {
            return customData.getString("id").orElse("");
        }
        return "";
    }

    public static void clickSlot(int slotIdx) {
        if (mc.interactionManager == null || mc.player == null) {
            return;
        }

        mc.interactionManager.clickSlot(
                mc.player.currentScreenHandler.syncId,
                slotIdx,
                0,
                SlotActionType.PICKUP,
                mc.player
        );
    }

    public static String format(String string, Object... values) {
        StringBuilder builder = new StringBuilder();
        int index = 0;
        for (String section : Splitter.on("{}").split(string)) {
            builder.append(section);
            if (index < values.length) {
                builder.append(values[index]);
            }
            index++;
        }
        return builder.toString();
    }

    public static Optional<Integer> parseInt(String value) {
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public static int getVersionNumber(String version) {
        String[] numbers = version.split("\\.");
        if (numbers.length >= 3) {
            return parseInt(numbers[0]).orElse(0) * 1000 + parseInt(numbers[1]).orElse(0) * 100 + parseInt(numbers[2]).orElse(0);
        }
        return 0;
    }

    public static void infoLink(String message, String url) {
        ClickEvent click = new ClickEvent.OpenUrl(URI.create(url));
        infoRaw(Text.literal(message).setStyle(Style.EMPTY.withClickEvent(click)));
    }

    public static MutableText getTag() {
        return Text.literal("[GlowPlayer] ").withColor(0x5ca0bf);
    }

    public static void info(String message) {
        infoRaw(Text.literal(message));
    }

    public static void infoRaw(MutableText message) {
        if (message.getStyle() == null || message.getStyle().getColor() == null) {
            message = message.withColor(0xffffff);
        }
        mc.inGameHud.getChatHud().addMessage(getTag().append(message), null, glowPlayerIndicator);
    }

    public static String getSkyblockId(ItemStack stack) {
        return getSkyblockId(getCustomData(stack));
    }

    public static boolean hasRightClickAbility(ItemStack stack) {
        return !getRightClickAbility(stack).isEmpty();
    }

    /**
     * Removes Minecraft formatting codes (§X) from a string.
     * E.g., "§8§m0.005§r" -> "0.005"
     * Matches any character after § (color codes, modifiers, reset, etc.)
     */
    public static String cleanFormatting(String text) {
        return text.replaceAll("§.", "");
    }

    /**
     * Checks if shift key is currently pressed using GLFW.
     * Works with both LEFT_SHIFT and RIGHT_SHIFT.
     */
    public static boolean isShiftKeyPressed() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.getWindow() == null) return false;

            long window = client.getWindow().getHandle();
            int shiftLeft = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT);
            int shiftRight = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT);

            return shiftLeft == GLFW.GLFW_PRESS || shiftRight == GLFW.GLFW_PRESS;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Formats a number with thousand separators.
     * E.g., 1234 -> "1,234"
     */
    public static String formatNumberWithCommas(int number) {
        return String.format("%,d", number);
    }
}
