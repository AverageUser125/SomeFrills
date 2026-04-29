package com.somefrills.features.misc.glowmob.chestui;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import java.util.List;

public interface UIAddon {
    // Allows the addon to filter or slice the item list
    void processItems(ChestUI ui, List<ItemStack> items);

    // Allows the addon to place its own buttons (arrows, compass, etc.)
    void drawDecoration(ChestUI ui, Inventory inventory);

    // Allows the addon to intercept clicks
    boolean onClick(ChestUI ui, ItemStack stack, String name, int button);
}