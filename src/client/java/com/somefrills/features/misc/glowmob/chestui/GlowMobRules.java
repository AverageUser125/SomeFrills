package com.somefrills.features.misc.glowmob.chestui;

import com.somefrills.config.Features;
import com.somefrills.config.misc.MobGlowConfig.GlowMobRule;
import com.somefrills.features.misc.glowmob.GlowMob;
import com.somefrills.features.misc.glowmob.MatchInfo;
import com.somefrills.misc.Utils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class GlowMobRules extends ChestUI {
    private final List<GlowMobRule> allRules;
    private RuleEditSession session;

    public GlowMobRules() {
        super("GlowMob Rules");
        allRules = Features.get(GlowMob.class).getRules();
        rebuild();
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    @Override
    protected void build() {
        nextSlot = 0;
        for (int i = 0; i < allRules.size(); i++) {
            GlowMobRule rule = allRules.get(i);
            ItemStack stack = new ItemStack(rule.enabled() ? Items.GREEN_TERRACOTTA : Items.RED_TERRACOTTA);
            // Display the rule index (1-based) in the menu
            String displayId = "Rule " + (i + 1);
            Utils.setCustomName(stack, colorStyle(rule.enabled() ? Formatting.GREEN : Formatting.RED).withItalic(false), displayId);
            stack.set(DataComponentTypes.LORE, getRuleLore(rule));
            addItem(stack);
        }

        ItemStack createButton = new ItemStack(Items.YELLOW_TERRACOTTA);
        Utils.setCustomName(createButton, colorStyle(Formatting.GREEN).withItalic(false), "Create Rule");

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal("Setup a new ").setStyle(colorStyle(Formatting.GRAY))
                .append(Text.literal("Glow").setStyle(colorStyle(Formatting.RED)))
                .append(Text.literal(" rule.").setStyle(colorStyle(Formatting.GRAY))));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Click to create!").setStyle(colorStyle(Formatting.YELLOW)));
        createButton.set(DataComponentTypes.LORE, new LoreComponent(lore, lore));

        addItem(createButton);
    }

    private LoreComponent getRuleLore(GlowMobRule rule) {
        MatchInfo matchInfo = rule.info();
        if (matchInfo == null) {
            return new LoreComponent(List.of(Text.of("No matcher info")), List.of(Text.of("No matcher info")));
        }

        List<Text> lines = new ArrayList<>();

        lines.add(Text.literal("Conditions: ").setStyle(colorStyle(Formatting.GREEN)));

        if (!matchInfo.type.isEmpty()) {
            String typeDisplay = Utils.capitalizeType(matchInfo.type);
            lines.add(Text.literal("Type: ").setStyle(colorStyle(Formatting.GRAY))
                    .append(Text.literal(typeDisplay).setStyle(colorStyle(Formatting.YELLOW))));
        }

        if (!matchInfo.name.isEmpty()) {
            lines.add(Text.literal("Name: ").setStyle(colorStyle(Formatting.GRAY))
                    .append(Text.literal(matchInfo.name).setStyle(colorStyle(Formatting.YELLOW))));
        }

        if (matchInfo.area != null) {
            lines.add(Text.literal("Island: ").setStyle(colorStyle(Formatting.GRAY))
                    .append(Text.literal(matchInfo.area.getDisplayName()).setStyle(colorStyle(matchInfo.area.getColorHex()))));
        }

        if (!matchInfo.gear.isEmpty()) {
            String gearStr = String.join(", ", matchInfo.gear.stream().map(Enum::name).toList());
            lines.add(Text.literal("Gear: ").setStyle(colorStyle(Formatting.GRAY))
                    .append(Text.literal(gearStr).setStyle(colorStyle(Formatting.YELLOW))));
        }

        lines.add(Text.literal(""));
        lines.add(Text.literal("Color: ").setStyle(colorStyle(Formatting.YELLOW)));

        String colorDisplay = Utils.colorToString(rule.color());
        lines.add(Text.literal(capitalize(colorDisplay)).setStyle(colorStyle(rule.color().hex)));

        lines.add(Text.literal(""));
        lines.add(Text.literal("Enabled: ").setStyle(colorStyle(Formatting.YELLOW))
                .append(Text.literal(rule.enabled() ? "On" : "Off").setStyle(colorStyle(rule.enabled() ? Formatting.GREEN : Formatting.RED))));

        lines.add(Text.literal("Right-click to toggle!").setStyle(colorStyle(Formatting.YELLOW)));
        lines.add(Text.literal("Left-click to configure!").setStyle(colorStyle(Formatting.YELLOW)));

        return new LoreComponent(lines, lines);
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
        if (itemName.isEmpty()) return;

        if (itemName.equals("Create Rule")) {
            createNewRule();
            return;
        }
        // Expecting display name like "Rule {index}"; parse index and use index-based lookup
        Integer parsedId = null;
        if (itemName.startsWith("Rule ")) {
            try {
                parsedId = Integer.parseInt(itemName.substring(5).trim());
            } catch (NumberFormatException ignored) {
            }
        } else {
            try {
                parsedId = Integer.parseInt(itemName.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        if (parsedId == null) return;

        if (parsedId < 1 || parsedId > allRules.size()) return;
        GlowMobRule rule = allRules.get(parsedId - 1);
        if (rule == null) return;

        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            Features.get(GlowMob.class).toggleRule(parsedId);
            rebuild();
            return;
        }
        // both middle-click and left-click will open the editor for the rule
        openRuleEditor(rule);
    }

    @Override
    protected void onReturn() {
        GlowMob glowMob = Features.get(GlowMob.class);

        if (session == null) {
            rebuild();
            return;
        }
        if (session.isNew()) {
            if (!session.workingCopy.info().isEmpty()) {
                glowMob.addRule(session.workingCopy);
            }
        } else {
            if (session.workingCopy.info().isEmpty()) {
                glowMob.removeRule(session.original);
            } else {
                glowMob.replaceRule(session.original, session.workingCopy);
            }
        }

        session = null;
        rebuild();
    }

    private void openRuleEditor(GlowMobRule rule) {
        session = new RuleEditSession(rule);
        Utils.setScreen(new GlowMobEditMenu(this, session.workingCopy));
    }

    private void createNewRule() {
        session = new RuleEditSession(null);
        Utils.setScreen(new GlowMobEditMenu(this, session.workingCopy));
    }

    private static class RuleEditSession {
        GlowMobRule original;        // null if creating new
        GlowMobRule workingCopy;

        RuleEditSession(GlowMobRule original) {
            this.original = original;
            if (original == null) {
                this.workingCopy = new GlowMobRule();
            } else {
                // editing existing rule: create a copy so edits can be committed or discarded
                this.workingCopy = new GlowMobRule(original);
                this.workingCopy.recompilePredicate(); // ensure predicate is recompiled for the copy, since it will be modified
            }
        }

        boolean isNew() {
            return original == null;
        }
    }
}

