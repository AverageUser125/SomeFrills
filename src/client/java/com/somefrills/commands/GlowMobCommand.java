package com.somefrills.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.somefrills.config.Features;
import com.somefrills.features.misc.GlowMob;
import com.somefrills.features.misc.matcher.Matcher;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Utils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class GlowMobCommand {

    public static LiteralArgumentBuilder<FabricClientCommandSource> getBuilder() {
        return literal("glowmob")
                .executes(ctx -> {
                    Utils.info("Usage: /glowmob <add|remove|list|clear>");
                    return 1;
                })
                .then(literal("list").executes(GlowMobCommand::listRules))
                .then(literal("clear").executes(ctx -> {
                    get().clearRules();
                    ctx.getSource().sendFeedback(net.minecraft.text.Text.literal("Cleared all entity highlight rules."));
                    return 1;
                }))
                .then(literal("add")
                        .then(argument("id", StringArgumentType.word())
                                .then(argument("matcher", MatcherArgumentType.matcher())
                                        .then(literal("color")
                                                .then(CommandColorUtils.buildColorArguments(GlowMobCommand::addRuleCommand))
                                        )
                                )
                        )
                )
                .then(literal("remove")
                        .then(argument("id", StringArgumentType.word())
                                .executes(GlowMobCommand::removeRuleCommand)
                        )
                );
    }

    private static int addRuleCommand(CommandContext<FabricClientCommandSource> ctx, RenderColor color) {
        String id = StringArgumentType.getString(ctx, "id");
        Matcher matcher = MatcherArgumentType.getMatcher(ctx, "matcher");

        boolean added = get().addRule(id, matcher, color);
        if (added) {
            ctx.getSource().sendFeedback(net.minecraft.text.Text.literal("Added rule '" + id + "' with matcher: " + matcher));
        } else {
            ctx.getSource().sendError(net.minecraft.text.Text.literal("Rule '" + id + "' already exists."));
        }
        return added ? 1 : 0;
    }

    private static int removeRuleCommand(CommandContext<FabricClientCommandSource> ctx) {
        String id = StringArgumentType.getString(ctx, "id");
        boolean removed = get().removeRule(id);
        if (removed) {
            ctx.getSource().sendFeedback(net.minecraft.text.Text.literal("Removed rule '" + id + "'."));
        } else {
            ctx.getSource().sendError(net.minecraft.text.Text.literal("Rule '" + id + "' not found."));
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
            for (GlowMob.GlowMobRule rule : rules) {
                String color = String.format("#%06X", rule.color().hex);
                sb.append("  • id=").append(rule.id())
                        .append(", color=").append(color)
                        .append("\n");
            }
        }

        ctx.getSource().sendFeedback(net.minecraft.text.Text.literal(sb.toString()));
        return 1;
    }

    private static GlowMob get() {
        return Features.get(GlowMob.class);
    }
}

