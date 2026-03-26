package com.somefrills.features.tweaks;

import com.google.common.collect.Sets;
import com.somefrills.config.Feature;
import com.somefrills.misc.Utils;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;

import static com.somefrills.Main.mc;

public class MiddleClickOverride {
    public static final Feature instance = new Feature("middleClickOverride", true);
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

    public static boolean shouldOverride(Slot slot, int button, ClickType actionType) {
        if (instance.isActive() && mc.screen instanceof ContainerScreen container) {
            if (slot != null && button == GLFW.GLFW_MOUSE_BUTTON_LEFT && actionType.equals(ClickType.PICKUP)) {
                String title = container.getTitle().getString();
                ItemStack stack = slot.getItem();
                if (stack.isEmpty() || isBlacklisted(title)) {
                    return false;
                }
                return Utils.getSkyblockId(stack).isEmpty() || isWhitelisted(title) || isTransaction(stack);
            }
        }
        return false;
    }
}
