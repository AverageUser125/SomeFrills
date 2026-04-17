package com.somefrills.chestui;

import com.somefrills.config.misc.MobGlowConfig.GlowMobRule;
import com.somefrills.features.misc.matcher.MatchInfo;
import com.somefrills.misc.MyMapColor;
import com.somefrills.misc.Utils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GlowMobEditMenu extends ChestUI {
    private final GlowMobRule rule;
    private final MatchInfo info;

    public GlowMobEditMenu(ChestUI previousMenu, GlowMobRule rule) {
        super("GlowMob Edit Menu", previousMenu);
        this.rule = rule;
        this.info = rule.info();
        rebuild();
    }

    @Override
    protected void build() {
        addItem(createChoiceItem(Items.CREEPER_SPAWN_EGG, "Entity",
                Utils.capitalizeType(info.type),
                "Click to choose entity to glow!"));

        addItem(createChoiceItem(Items.NAME_TAG, "Name",
                info.name,
                "Click to set name filter!"));

        addItem(createChoiceItem(Items.CARVED_PUMPKIN, "Area",
                info.area != null ? info.area.getDisplayName() : null,
                info.area != null ? info.area.getColorHex() : null,
                "Click to select area!"));

        addItem(createGearChoiceItem());

        var delete = new ItemStack(Items.CAULDRON);
        Utils.setCustomName(delete, colorStyle(Formatting.RED), "Delete");
        getInventory().setStack(INV_SIZE - 9 + 5, delete);

        addItem(createChoiceItem(MyMapColor.getClosest(rule.color()).getItem(), "Color",
                Utils.colorToString(rule.color()),
                rule.color().hex,
                "Click to select glow color!"));
    }

    private ItemStack createGearChoiceItem() {
        String text;
        int color;

        Set<MatchInfo.GearFlag> gear = info.gear;

        // NONE
        if (gear.isEmpty()) {
            text = "None";
            color = Formatting.RED.getColorValue();
            return createChoiceItem(
                    Items.IRON_CHESTPLATE,
                    "Gear",
                    text,
                    color,
                    "Click to toggle armor filter!"
            );
        }

        // NAKED overrides everything
        if (gear.contains(MatchInfo.GearFlag.NAKED)) {
            text = "Naked";
            return createChoiceItem(
                    Items.CHAINMAIL_CHESTPLATE,
                    "Gear",
                    text,
                    "Click to toggle armor filter!"
            );
        }

        // NORMAL ARMOR SELECTION
        List<String> parts = new ArrayList<>();

        if (gear.contains(MatchInfo.GearFlag.CHEST)) parts.add("Chestplate");
        if (gear.contains(MatchInfo.GearFlag.LEGS))  parts.add("Leggings");
        if (gear.contains(MatchInfo.GearFlag.FEET))  parts.add("Boots");
        if (gear.contains(MatchInfo.GearFlag.HEAD))  parts.add("Helmet");

        text = String.join(", ", parts);

        color = parts.isEmpty()
                ? Formatting.RED.getColorValue()
                : Formatting.GREEN.getColorValue();

        return createChoiceItem(
                Items.IRON_CHESTPLATE,
                "Gear",
                text,
                color,
                "Click to toggle armor filter!"
        );
    }

    private ItemStack createChoiceItem(Item item, String label, String chosen, String bottomText) {
        return createChoiceItem(item, label, chosen, Formatting.YELLOW.getColorValue(), bottomText);
    }

    private ItemStack createChoiceItem(Item item, String label, String chosen, Integer chosenColor, String bottomText) {
        ItemStack stack = new ItemStack(item);
        Utils.setCustomName(stack, colorStyle(Formatting.GREEN).withItalic(false), label);

        List<Text> lore = new ArrayList<>();
        if (chosen != null && !chosen.isEmpty()) {
            lore.add(Text.literal("Chosen: ").setStyle(colorStyle(Formatting.GRAY))
                    .append(Text.literal(chosen).setStyle(colorStyle(chosenColor))));
        } else {
            lore.add(Text.literal("Chosen: ").setStyle(colorStyle(Formatting.GRAY))
                    .append(Text.literal("None").setStyle(colorStyle(Formatting.RED))));
        }
        lore.add(Text.literal(""));
        lore.add(Text.literal(bottomText).setStyle(colorStyle(Formatting.YELLOW)));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore, lore));
        stack.remove(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        return stack;
    }

    private Style colorStyle(Formatting color) {
        Integer colorValue = color.getColorValue();
        if (colorValue == null) {
            return Style.EMPTY;
        }
        return colorStyle(colorValue);
    }

    private Style colorStyle(int colorHex) {
        return Style.EMPTY.withColor(TextColor.fromRgb(colorHex));
    }

    @Override
    protected void onItemClick(ItemStack stack, int button) {
        if (stack.isEmpty()) return;

        String itemName = Utils.getPlainCustomName(stack);
        switch (itemName) {
            case "Entity" -> Utils.setScreen(new EntityTypesMenu(this, info));
            case "Area" -> Utils.setScreen(new AreaSelectionMenu(this, info));
            case "Gear" -> Utils.setScreen(new ArmorSelectionMenu(this, info));
            case "Color" -> Utils.setScreen(new ColorSelectionMenu(this, rule.color()));
            case "Name" -> SignGui.open("Set Name Filter", lines -> {
                // concat all lines except first one, IGNORE THE FIRST LINE
                // note: lines is a String[]
                StringBuilder nameBuilder = new StringBuilder();
                for (int i = 1; i < lines.length; i++) {
                    if (i > 1) nameBuilder.append(" ");
                    nameBuilder.append(lines[i]);
                }
                info.name = nameBuilder.toString().trim();
                rebuild();
                Utils.setScreen(this);
            });
            case "Delete" -> {
                info.clear();
                close();
            }
        }
    }
}
