package com.somefrills.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.somefrills.features.core.Features;
import com.somefrills.features.misc.glowmob.GlowMob;
import com.somefrills.features.misc.glowmob.MatchInfo;
import com.somefrills.features.misc.glowmob.chestui.GlowMobRules;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Utils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;

import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class GlowMobCommand {

    public static LiteralArgumentBuilder<FabricClientCommandSource> getBuilder() {
        return literal("glowmob")
                .executes(ctx -> {
                    //Utils.info("Usage: /glowmob <add|remove|list|clear>");
                    if (!get().isActive()) {
                        ctx.getSource().sendError(Text.literal("GlowMob feature is not active."));
                        return 0;
                    }
                    Utils.setScreen(new GlowMobRules());
                    return 1;
                })
                .then(literal("list").executes(GlowMobCommand::listRules))
                .then(literal("listglowing")
                        .executes(GlowMobCommand::listGlowingMobs)
                        .then(argument("id", IntegerArgumentType.integer(1))
                                .executes(ctx ->
                                        listGlowingMobs(
                                                ctx,
                                                IntegerArgumentType.getInteger(ctx, "id")
                                        )
                                )
                        )
                )
                .then(literal("clear").executes(ctx -> {
                    get().clearRules();
                    ctx.getSource().sendFeedback(Text.literal("Cleared all entity highlight rules."));
                    return 1;
                }))
                .then(literal("add")
                        .then(CommandColorUtils.buildColorArgument()
                                .then(argument("matcher", MatcherArgumentType.matcher())
                                        .executes(ctx -> {
                                            RenderColor color = CommandColorUtils.getColorArgument(ctx);
                                            MatchInfo matcher = MatcherArgumentType.getMatcher(ctx, "matcher");
                                            return addRuleCommand(ctx, color, matcher);
                                        })
                                )
                        )
                )
                .then(literal("remove")
                        .then(literal("all")
                                .executes(ctx -> {
                                    get().clearRules();
                                    ctx.getSource().sendFeedback(Text.literal("Cleared all entity highlight rules."));
                                    return 1;
                                })
                        )
                        .then(argument("id", IntegerArgumentType.integer(1))
                                .executes(GlowMobCommand::removeRuleCommand)
                        )
                );
    }

    private static int listGlowingMobs(CommandContext<FabricClientCommandSource> ctx) {
        return listGlowingMobs(ctx, -1); // -1 = all rules
    }

    private static int listGlowingMobs(CommandContext<FabricClientCommandSource> ctx, int ruleId) {
        var manager = get();
        var rules = manager.getRules();

        if (rules.isEmpty()) {
            ctx.getSource().sendFeedback(Text.literal("No glow rules found."));
            return 1;
        }

        // Validate rule id if provided
        if (ruleId != -1 && (ruleId < 1 || ruleId > rules.size())) {
            ctx.getSource().sendFeedback(Text.literal("Invalid rule id."));
            return 0;
        }

        StringBuilder sb = new StringBuilder();

        // If specific rule requested, pass only that rule
        var entries = (ruleId == -1)
                ? manager.getGlowingMobs()
                : manager.getGlowingMobs(List.of(rules.get(ruleId - 1)));

        for (var entry : entries) {
            var rule = entry.rule;
            List<LivingEntity> entities = entry.entities;

            int currentRuleId = rules.indexOf(rule) + 1;

            sb.append("Rule ")
                    .append(currentRuleId)
                    .append(" (")
                    .append(Utils.colorToString(rule.color()))
                    .append("):\n");

            if (entities.isEmpty()) {
                sb.append("  - No matching glowing mobs\n");
                continue;
            }

            for (var entity : entities) {
                sb.append("  - ")
                        .append(Utils.toPlain(entity.getName()))
                        .append(" (")
                        .append(entity.getType().getName().getString())
                        .append(")")
                        .append(", MaxHP: ")
                        .append(Utils.formatCompact(entity.getMaxHealth()))
                        .append(", Pos: [")
                        .append(Utils.formatCompact(entity.getX()))
                        .append(", ")
                        .append(Utils.formatCompact(entity.getY()))
                        .append(", ")
                        .append(Utils.formatCompact(entity.getZ()))
                        .append("]\n");
            }
        }

        ctx.getSource().sendFeedback(Text.literal(sb.toString()));
        return 1;
    }


    private static int addRuleCommand(CommandContext<FabricClientCommandSource> ctx, RenderColor color, MatchInfo matcher) {
        int addIndex = get().addRule(matcher, color);
        if (addIndex != -1) {
            ctx.getSource().sendFeedback(Text.literal("Added rule " + addIndex + " with color " + Utils.colorToString(color) + " and matcher: " + matcher.serialize()));
        } else {
            ctx.getSource().sendError(Text.literal("Some error"));
        }
        return addIndex == -1 ? 1 : 0;
    }

    private static int removeRuleCommand(CommandContext<FabricClientCommandSource> ctx) {
        int id = IntegerArgumentType.getInteger(ctx, "id");
        boolean removed = get().removeRule(id);
        if (removed) {
            ctx.getSource().sendFeedback(Text.literal("Removed rule '" + id + "'."));
        } else {
            ctx.getSource().sendError(Text.literal("Rule '" + id + "' not found."));
        }
        return removed ? 1 : 0;
    }

    private static int listRules(CommandContext<FabricClientCommandSource> ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Entity Highlight Rules ===\n");

        var rules = get().getRules();
        if (rules.isEmpty()) {
            sb.append("  (none)\n");
        } else {
            for (int idx = 0; idx < rules.size(); idx++) {
                var rule = rules.get(idx);
                String color = Utils.colorToString(rule.color());
                sb.append("  • id=").append(idx + 1)
                        .append(", color=").append(color)
                        .append("\n");
            }
        }

        ctx.getSource().sendFeedback(Text.literal(sb.toString()));
        return 1;
    }

    private static GlowMob get() {
        return Features.get(GlowMob.class);
    }
}
