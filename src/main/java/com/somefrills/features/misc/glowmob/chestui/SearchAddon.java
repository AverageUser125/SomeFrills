package com.somefrills.features.misc.glowmob.chestui;

import com.somefrills.misc.Utils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

import static com.somefrills.features.misc.glowmob.chestui.ChestUI.INV_SIZE;

public class SearchAddon implements UIAddon {
    private String searchQuery = null;

    public int searchSlot = INV_SIZE - 9 + 2;
    public int clearSlot = INV_SIZE - 9 + 1;

    @Override
    public void processItems(ChestUI ui, List<ItemStack> items) {
        if (searchQuery == null || searchQuery.isEmpty()) return;

        String query = searchQuery.toLowerCase();
        items.removeIf(stack -> {
            String name = Utils.getPlainCustomName(stack);
            return name == null || !name.toLowerCase().contains(query);
        });
    }

    @Override
    public void drawDecoration(ChestUI ui, Inventory inventory) {
        // --- Search Compass ---
        ItemStack searchItem = new ItemStack(Items.COMPASS);
        Utils.setCustomName(searchItem, colorStyle(Formatting.AQUA).withItalic(false), "Search");

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal("Current Filter: ").setStyle(colorStyle(Formatting.GRAY))
                .append(Text.literal(searchQuery == null ? "None" : searchQuery)
                        .setStyle(colorStyle(searchQuery == null ? Formatting.RED : Formatting.YELLOW))));

        lore.add(Text.literal(""));
        lore.add(Text.literal("Click to filter results").setStyle(colorStyle(Formatting.YELLOW)));

        searchItem.set(DataComponentTypes.LORE, new LoreComponent(lore, lore));
        inventory.setStack(searchSlot, searchItem);

        // --- Clear Search (Oak Sign) ---
        if (searchQuery != null && !searchQuery.isEmpty()) {
            ItemStack clearItem = new ItemStack(Items.OAK_SIGN);
            Utils.setCustomName(clearItem, colorStyle(Formatting.RED).withItalic(false), "Clear Search");

            List<Text> clearLore = new ArrayList<>();
            clearLore.add(Text.literal("Reset the search filter").setStyle(colorStyle(Formatting.GRAY)));
            clearItem.set(DataComponentTypes.LORE, new LoreComponent(clearLore, clearLore));

            inventory.setStack(clearSlot, clearItem);
        }
    }

    @Override
    public boolean onClick(ChestUI ui, ItemStack stack, String name, int button) {
        if (name.equals("Search")) {
            // Line 1: Header, Line 2: Current Query
            String[] signText = new String[]{"Enter Query", searchQuery == null ? "" : searchQuery, "", ""};

            SignGui.open(signText, lines -> {
                // We take the input from the second line (index 1)
                String input = lines[1].trim();
                this.searchQuery = input.isEmpty() ? null : input;

                ui.rebuild();
                Utils.setScreen(ui);
            });
            return true;
        }

        if (name.equals("Clear Search")) {
            this.searchQuery = null;
            ui.rebuild();
            return true;
        }
        return false;
    }

    private Style colorStyle(Formatting color) {
        Integer colorValue = color.getColorValue();
        return colorValue == null ? Style.EMPTY : Style.EMPTY.withColor(TextColor.fromRgb(colorValue));
    }
}