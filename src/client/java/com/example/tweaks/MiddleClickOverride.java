package com.example.tweaks;

import com.example.utils.AllConfig;
import com.example.utils.Utils;
import com.google.common.collect.Sets;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;

import static com.example.Main.mc;

public class MiddleClickOverride {
    private static final HashSet<String> matchBlacklist = Sets.newHashSet(
            "Attribute Fusion",
            "Beacon",
            "Chest",
            "Large Chest",
            "Anvil",
            "Storage",
            "Drill Anvil",
            "Runic Pedestal",
            "Rune Removal",
            "Reforge Anvil",
            "Reforge Item",
            "Offer Pets",
            "Exp Sharing",
            "Convert to Dungeon Item",
            "Upgrade Item",
            "Salvage Items",
            "AⒷiphone",
            "Fishing Rod Parts",
            "Stats Tuning",
            "Pet Sitter",
            "Transfer to Profile",
            "Attribute Transfer",
            "Hunting Box"
    );
    private static final HashSet<String> matchWhitelist = Sets.newHashSet(
            "Your Equipment and Stats",
            "Accessory Bag Thaumaturgy",
            "Community Shop"
    );
    private static final HashSet<String> containBlacklist = Sets.newHashSet(
            "Wardrobe",
            "Minion",
            "Abiphone",
            "The Hex",
            "Enchant Item",
            "Auction",
            "Cosmetic",
            "Trap",
            "Gemstone",
            "Heart of the",
            "Widgets"
    );
    private static final HashSet<String> containWhitelist = Sets.newHashSet(
            "Pets",
            "Bits Shop"
    );


    private static boolean isBlacklisted(String title) {
        return matchBlacklist.contains(title) || containBlacklist.stream().anyMatch(title::contains);
    }

    private static boolean isWhitelisted(String title) {
        return matchWhitelist.contains(title) || containWhitelist.stream().anyMatch(title::contains);
    }

    private static boolean isTransaction(ItemStack stack) {
        return Utils.getLoreLines(stack).stream().anyMatch(line -> line.equals("Cost") || line.equals("Sell Price") || line.equals("Bazaar Price"));
    }

    public static boolean shouldOverride(Slot slot, int button, SlotActionType actionType) {
        if (AllConfig.middleClickOverride && mc.currentScreen instanceof GenericContainerScreen container) {
            if (slot != null && button == GLFW.GLFW_MOUSE_BUTTON_LEFT && actionType.equals(SlotActionType.PICKUP)) {
                String title = container.getTitle().getString();
                ItemStack stack = slot.getStack();
                if (stack.isEmpty() || isBlacklisted(title)) {
                    return false;
                }
                return Utils.getSkyblockId(stack).isEmpty() || isWhitelisted(title) || isTransaction(stack);
            }
        }
        return false;
    }
}
