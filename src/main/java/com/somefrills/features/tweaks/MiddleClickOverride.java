package com.somefrills.features.tweaks;

import com.google.common.collect.Sets;
import com.somefrills.config.FrillsConfig;
import com.somefrills.features.core.Feature;
import com.somefrills.misc.Utils;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;

import static com.somefrills.Main.mc;

public class MiddleClickOverride extends Feature {
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
            Utils.format("A{}iphone", Utils.Symbols.bingo),
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

    public MiddleClickOverride() {
        super(FrillsConfig.instance.tweaks.middleClickOverrideEnabled);
    }

    private static boolean isLeftClick(int button, SlotActionType actionType) {
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT && actionType.equals(SlotActionType.PICKUP);
    }

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
        if (!FrillsConfig.instance.tweaks.middleClickOverrideEnabled.get()) return false;
        if (!(mc.currentScreen instanceof GenericContainerScreen container)) return false;
        if (slot == null) return false;
        if (!isLeftClick(button, actionType)) return false;

        String title = container.getTitle().getString();
        ItemStack stack = slot.getStack();

        if (stack.isEmpty()) return false;
        if (isBlacklisted(title)) return false;
        if (!Utils.isInSkyblock()) return false;

        return Utils.getSkyblockId(stack).isEmpty() || isWhitelisted(title) || isTransaction(stack);
    }
}
