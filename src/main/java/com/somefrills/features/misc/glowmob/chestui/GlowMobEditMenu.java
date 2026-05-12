package com.somefrills.features.misc.glowmob.chestui;

import com.somefrills.config.misc.GlowMobConfig.GlowMobRule;
import com.somefrills.features.misc.glowmob.MatchInfo;
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
    private boolean revertRequested = false;

    public GlowMobEditMenu(ChestUI previousMenu, GlowMobRule rule) {
        super("GlowMob Edit Menu", previousMenu);
        this.rule = rule;
        this.info = rule.info();
        rebuild();
    }

    public boolean isRevertRequested() {
        return revertRequested;
    }

    @Override
    protected void build() {
        addItem(createChoiceItem(MyMapColor.getClosest(rule.color()).getItem(), "Color",
                Utils.colorToString(rule.color()),
                rule.color().hex,
                "Sets the glow color",
                "Click to change color"));

        addItem(createChoiceItem(Items.CREEPER_SPAWN_EGG, "Entity",
                Utils.wrapByDelimiter(info.type.toString(), 20, ","),
                "Filters by mob type (zombie, creeper, etc.)",
                "Leave empty to match all types",
                "Click to choose entities"));

        addItem(createChoiceItem(Items.NAME_TAG, "Name",
                info.name,
                "Matches custom names from armor stands",
                "Searches nearby name tags above mobs",
                "Click to set name filter"));

        addItem(createChoiceItem(Items.CARVED_PUMPKIN, "Area",
                info.area != null ? info.area.getDisplayName() : null,
                info.area != null ? info.area.getColorHex() : null,
                "Only glows mobs in specific areas",
                "Leave empty for all locations",
                "Click to select area"));

        addItem(createGearChoiceItem());

        addItem(createChoiceItem(Items.ENCHANTED_BOOK, "Max HP",
                info.maxHp > 0 ? Utils.formatCompact(info.maxHp) : null,
                "Only glows mobs with this exact max health",
                "Leave empty for any health",
                "Click to set max HP filter"));

        // Add revert button if this is an edit session (not creating new)
        if (previousScreen instanceof GlowMobRules) {
            var revert = new ItemStack(Items.REDSTONE);
            Utils.setCustomName(revert, colorStyle(Formatting.GOLD), "Revert Changes");
            List<Text> revertLore = new ArrayList<>();
            revertLore.add(Text.literal("Restore to original").setStyle(colorStyle(Formatting.GRAY)));
            revertLore.add(Text.literal("").setStyle(colorStyle(Formatting.GRAY)));
            revertLore.add(Text.literal("Click to revert").setStyle(colorStyle(Formatting.YELLOW)));
            revert.set(DataComponentTypes.LORE, new LoreComponent(revertLore, revertLore));
            getInventory().setStack(INV_SIZE - 9 + 3, revert);
        }

        var delete = new ItemStack(Items.CAULDRON);
        Utils.setCustomName(delete, colorStyle(Formatting.RED), "Delete");
        getInventory().setStack(INV_SIZE - 9 + 5, delete);
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
                    "Filters by equipped armor",
                    "Leave empty for any armor state",
                    "Click to toggle armor filter"
            );
        }

        // NAKED overrides everything
        if (gear.contains(MatchInfo.GearFlag.NAKED)) {
            text = "Naked";
            return createChoiceItem(
                    Items.CHAINMAIL_CHESTPLATE,
                    "Gear",
                    text,
                    "Filters by equipped armor",
                    "Only matches mobs with no armor",
                    "Click to toggle armor filter"
            );
        }

        // NORMAL ARMOR SELECTION
        List<String> parts = new ArrayList<>();

        if (gear.contains(MatchInfo.GearFlag.CHEST)) parts.add("Chestplate");
        if (gear.contains(MatchInfo.GearFlag.LEGS)) parts.add("Leggings");
        if (gear.contains(MatchInfo.GearFlag.FEET)) parts.add("Boots");
        if (gear.contains(MatchInfo.GearFlag.HEAD)) parts.add("Helmet");

        text = String.join(", ", parts);

        color = parts.isEmpty()
                ? Formatting.RED.getColorValue()
                : Formatting.GREEN.getColorValue();

        return createChoiceItem(
                Items.IRON_CHESTPLATE,
                "Gear",
                text,
                color,
                "Filters by equipped armor",
                "Matches mobs wearing selected pieces",
                "Click to toggle armor filter"
        );
    }

    private ItemStack createChoiceItem(Item item, String label, String chosen, String... descriptions) {
        return createChoiceItem(item, label, chosen, Formatting.YELLOW.getColorValue(), descriptions);
    }

    private ItemStack createChoiceItem(Item item, String label, String chosen, Integer chosenColor, String... descriptions) {
        ItemStack stack = new ItemStack(item);
        Utils.setCustomName(stack, colorStyle(Formatting.GREEN).withItalic(false), label);

        List<Text> lore = new ArrayList<>();

        // Display chosen value
        if (chosen != null && !chosen.isEmpty()) {
            String[] lines = chosen.split("\n");
            lore.add(Text.literal("Chosen: ").setStyle(colorStyle(Formatting.GRAY))
                    .append(Text.literal(lines[0]).setStyle(colorStyle(chosenColor))));
            for (int i = 1; i < lines.length; i++) {
                lore.add(Text.literal(lines[i]).setStyle(colorStyle(chosenColor)));
            }
        } else {
            lore.add(Text.literal("Chosen: ").setStyle(colorStyle(Formatting.GRAY))
                    .append(Text.literal("(Unset)").setStyle(colorStyle(Formatting.DARK_GRAY))));
        }

        lore.add(Text.literal(""));

        // Add descriptions with proper formatting
        if (descriptions.length > 0) {
            for (int i = 0; i < descriptions.length; i++) {
                String desc = descriptions[i];
                if (i == descriptions.length - 1) {
                    // Last line: action text in YELLOW
                    lore.add(Text.literal(desc).setStyle(colorStyle(Formatting.YELLOW)));
                } else {
                    // Middle lines: help text in GRAY
                    lore.add(Text.literal(desc).setStyle(colorStyle(Formatting.GRAY)));
                }
            }
        }

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
            case "Name" -> SignGui.open(new String[]{"Set Name Filter", info.name}, lines -> {
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
            case "Max HP" ->
                    SignGui.open(new String[]{"Enter Max Hp", info.maxHp > 0 ? Utils.formatCompact(info.maxHp) : ""}, lines -> {
                        if (lines.length < 2) return;
                        String input = lines[1].trim();

                        // If empty, clear the filter
                        if (input.isEmpty()) {
                            info.maxHp = 0;
                        } else {
                            try {
                                info.maxHp = Utils.parseCompact(input);
                            } catch (NumberFormatException e) {
                                // Invalid input - don't update
                                Utils.setScreen(this);
                                return;
                            }
                        }
                        rebuild();
                        Utils.setScreen(this);
                    });
            case "Revert Changes" -> {
                revertRequested = true;
                close();
            }
            case "Delete" -> {
                info.clear();
                close();
            }
        }
    }
}
