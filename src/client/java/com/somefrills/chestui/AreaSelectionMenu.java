package com.somefrills.chestui;

import com.somefrills.features.misc.matcher.MatchInfo;
import com.somefrills.misc.Area;
import com.somefrills.misc.Utils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;

public class AreaSelectionMenu extends ChestUI {
    private final MatchInfo info;

    public AreaSelectionMenu(ChestUI previousScreen, MatchInfo info) {
        super("Select Area", previousScreen);
        this.info = info;
        rebuild();
    }

    @Override
    protected void build() {
        for(Area area : Area.values()) {
            addItem(createChoiceItem(area.getItem(), area.getDisplayName(), area.getColorHex()));
        }
        addItem(createChoiceItem(Items.STRUCTURE_VOID, "None", 0xFF0000));
    }

    private ItemStack createChoiceItem(Item baseItem, String displayName, int colorHex) {
        ItemStack stack = baseItem.getDefaultStack();
        Utils.setCustomName(stack, Style.EMPTY.withColor(TextColor.fromRgb(colorHex)), displayName);
        return stack;
    }

    @Override
    protected void onItemClick(ItemStack stack, int button) {
        String customName = Utils.getPlainCustomName(stack);
        if (customName.equals("None")) {
            info.area = null;
        } else {
            info.area = Area.fromString(customName);
        }
        close();
    }
}
