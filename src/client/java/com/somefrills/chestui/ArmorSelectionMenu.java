package com.somefrills.chestui;

import com.somefrills.features.misc.matcher.MatchInfo;
import com.somefrills.features.misc.matcher.MatchInfo.GearFlag;
import com.somefrills.misc.Utils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;

import java.util.Set;

public class ArmorSelectionMenu extends ChestUI {

    private final Set<GearFlag> gear;

    public ArmorSelectionMenu(ChestUI previousScreen, MatchInfo info) {
        super("Select Armor Type", previousScreen);
        this.gear = info.gear;
        rebuild();
    }

    // ========================
    // Helpers
    // ========================

    private boolean is(GearFlag flag) {
        return gear.contains(flag);
    }

    private boolean isNaked() {
        return gear.contains(GearFlag.NAKED);
    }

    private boolean isNone() {
        return gear.isEmpty();
    }

    private void toggleArmor(GearFlag flag) {
        if (gear.contains(flag)) {
            gear.remove(flag);
            return;
        }

        // switching to armor removes naked
        gear.remove(GearFlag.NAKED);

        gear.add(flag);
    }

    private void toggleNaked() {
        if (gear.contains(GearFlag.NAKED)) {
            gear.remove(GearFlag.NAKED);
            return;
        }

        gear.clear();
        gear.add(GearFlag.NAKED);
    }

    // ========================
    // UI build
    // ========================

    @Override
    protected void build() {
        allItems.clear();

        boolean naked = isNaked();
        boolean none = isNone();

        addItem("Chestplate", GearFlag.CHEST, Items.LEATHER_CHESTPLATE,
                !naked && is(GearFlag.CHEST));

        addItem( "Leggings", GearFlag.LEGS, Items.LEATHER_LEGGINGS,
                !naked && is(GearFlag.LEGS));

        addItem( "Boots", GearFlag.FEET, Items.LEATHER_BOOTS,
                !naked && is(GearFlag.FEET));

        addItem( "Helmet", GearFlag.HEAD, Items.LEATHER_HELMET,
                !naked && is(GearFlag.HEAD));

        addItem("Naked", GearFlag.NAKED, Items.BARRIER, naked);

        addItem("None", null, Items.PAPER, none);
    }

    private void addItem(String name, GearFlag flag, Item itemType, boolean enabled) {
        ItemStack item = new ItemStack(itemType);

        int color = enabled ? 0x00FF00 : 0xFF5555;

        String status = enabled ? " [ON]" : " [OFF]";

        Utils.setCustomName(
                item,
                Style.EMPTY.withColor(TextColor.fromRgb(color)),
                name + status
        );

        DyedColorComponent colorComponent = new DyedColorComponent(color);
        item.set(DataComponentTypes.DYED_COLOR, colorComponent);
        item.remove(DataComponentTypes.ATTRIBUTE_MODIFIERS);

        if(flag != null) {
            NbtCompound data = new NbtCompound();
            data.putString("Flag", flag.name());
            Utils.setCustomData(item, data);
        }
        addItem(item);
    }

    // ========================
    // Click handling
    // ========================

    @Override
    protected void onItemClick(ItemStack stack, int button) {
        var customData = Utils.getCustomData(stack);
        if (customData == null) {
            gear.clear();
            rebuild();
            return;
        }
        String flagName = customData.get("Flag").asString().orElse(null);
        if (flagName == null) {
            gear.clear();
            rebuild();
            return;
        }

        GearFlag flag = GearFlag.valueOf(flagName);
        if (flag == GearFlag.NAKED) {
            toggleNaked();
        } else {
            toggleArmor(flag);
        }

        rebuild();
    }
}