package com.somefrills.chestui;

import com.somefrills.misc.MyMapColor;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Utils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Style;
import net.minecraft.util.DyeColor;

public class ColorSelectionMenu extends ChestUI{
    private final RenderColor color;

    public ColorSelectionMenu(ChestUI previousScreen, RenderColor initialColor) {
        super("Color", previousScreen);
        this.color = initialColor;
        rebuild();
    }

    @Override
    protected void build() {
        for (MyMapColor color : MyMapColor.values()) {
            ItemStack stack = new ItemStack(color.getItem());
            int colorHex = color.getColor();
            String name = Utils.capitalizeType(color.name().toLowerCase());
            Utils.setCustomName(stack, Style.EMPTY.withColor(colorHex), name);
            var nbt = new NbtCompound();
            nbt.putInt("color", colorHex);
            Utils.setCustomData(stack, nbt);
            addItem(stack);
        }
    }

    @Override
    protected void onItemClick(ItemStack stack, int button) {
        if (stack.isEmpty()) return;
        int colorHex = Utils.getCustomData(stack).getInt("color").orElse(0xFFFFFF);
        color.set(RenderColor.fromHex(colorHex));
        close();
    }
}
