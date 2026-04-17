package com.somefrills.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.somefrills.chestui.GlowMobRules;
import com.somefrills.config.Features;
import com.somefrills.features.misc.GlowMob;
import com.somefrills.features.misc.matcher.MatchInfo;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Utils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class GlowMobCommand {

    public static LiteralArgumentBuilder<FabricClientCommandSource> getBuilder() {
        return literal("glowmob")
                .executes(ctx -> {
                    //Utils.info("Usage: /glowmob <add|remove|list|clear>");
                    Utils.setScreen(new GlowMobRules());
                    return 1;
                })
                .then(literal("list").executes(GlowMobCommand::listRules))
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
                        .then(argument("id", IntegerArgumentType.integer(1))
                                .executes(GlowMobCommand::removeRuleCommand)
                        )
                );
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

