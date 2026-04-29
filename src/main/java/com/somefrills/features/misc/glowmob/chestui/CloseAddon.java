package com.somefrills.features.misc.glowmob.chestui;

import com.somefrills.misc.Utils;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

import java.util.List;

import static com.somefrills.features.misc.glowmob.chestui.ChestUI.INV_SIZE;

public class CloseAddon implements UIAddon {

    // Default slot is middle-bottom (Index 49 for a 9x6 chest)
    public int closeSlot = INV_SIZE - 9 + 4;

    @Override
    public void processItems(ChestUI ui, List<ItemStack> items) {
        // This addon doesn't filter or modify the content list
    }

    @Override
    public void drawDecoration(ChestUI ui, Inventory inventory) {
        if (ui.previousScreen == null) return; // Don't show close button if there's no previous screen to go back to
        ItemStack closeButton = new ItemStack(Items.BARRIER);
        Style barrierStyle = Style.EMPTY.withColor(Formatting.GRAY).withItalic(false);
        Utils.setCustomName(closeButton, barrierStyle, "Close");

        inventory.setStack(closeSlot, closeButton);
    }

    @Override
    public boolean onClick(ChestUI ui, ItemStack stack, String name, int button) {
        if (name.equals("Close")) {
            ui.close();
            return true;
        }
        return false;
    }
}