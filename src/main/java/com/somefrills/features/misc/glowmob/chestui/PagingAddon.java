package com.somefrills.features.misc.glowmob.chestui;

import com.somefrills.misc.Utils;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;

import java.util.ArrayList;
import java.util.List;

import static com.somefrills.features.misc.glowmob.chestui.ChestUI.INV_SIZE;

public class PagingAddon implements UIAddon {
    private int currentPage = 0;
    private int totalPages = 1;

    // Sensible Defaults
    public int prevSlot = INV_SIZE - 9 + 3;
    public int nextSlot = INV_SIZE - 9 + 5;
    public int perPage = 7 * 4;

    @Override
    public void processItems(ChestUI ui, List<ItemStack> items) {
        // Calculate total pages based on current items (filtered or otherwise)
        // Math.max(1, ...) ensures we don't have 0 pages, which simplifies logic
        totalPages = Math.max(1, (int) Math.ceil((double) items.size() / perPage));

        if (currentPage >= totalPages) currentPage = totalPages - 1;
        if (currentPage < 0) currentPage = 0;

        int start = currentPage * perPage;
        int end = Math.min(start + perPage, items.size());

        // If list is empty or start > size, subList might throw error, so we check
        if (start < items.size()) {
            List<ItemStack> pageItems = new ArrayList<>(items.subList(start, end));
            items.clear();
            items.addAll(pageItems);
        } else {
            items.clear();
        }
    }

    @Override
    public void drawDecoration(ChestUI ui, Inventory inventory) {
        // 1. Only show "Previous" if we aren't on the first page
        if (currentPage > 0) {
            ItemStack back = new ItemStack(Items.ARROW);
            Utils.setCustomName(back, Style.EMPTY, "Previous Page");
            inventory.setStack(prevSlot, back);
        }

        // 2. Only show "Next" if there is at least one page ahead of us
        // This implicitly handles the "Single Page" case: 0 < 1 - 1 is false.
        if (currentPage < totalPages - 1) {
            ItemStack forward = new ItemStack(Items.ARROW);
            Utils.setCustomName(forward, Style.EMPTY, "Next Page");
            inventory.setStack(nextSlot, forward);
        }
    }

    @Override
    public boolean onClick(ChestUI ui, ItemStack stack, String name, int button) {
        if (name.equals("Previous Page")) {
            if (currentPage > 0) {
                currentPage--;
                ui.rebuild();
            }
            return true;
        }
        if (name.equals("Next Page")) {
            if (currentPage < totalPages - 1) {
                currentPage++;
                ui.rebuild();
            }
            return true;
        }
        return false;
    }
}