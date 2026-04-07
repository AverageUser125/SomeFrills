package com.somefrills.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.somefrills.config.FrillsConfig;
import com.somefrills.features.misc.EntityHighlight;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Utils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.registry.Registries;

import java.util.concurrent.CompletableFuture;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class EntityHighlightCommand {

    public static LiteralArgumentBuilder<FabricClientCommandSource> getBuilder() {
        return literal("entityhighlight")
                .executes(ctx -> {
                    if (!isEntityHighlightEnabled()) {
                        Utils.info("EntityHighlight feature is disabled.");
                        return 1;
                    }
                    Utils.info("Usage: /entityhighlight <add|remove|list|clear>");
                    return 1;
                })
                .then(literal("list").executes(ctx -> {
                    if (!isEntityHighlightEnabled()) {
                        Utils.info("EntityHighlight feature is disabled.");
                        return 1;
                    }
                    listRules();
                    return 1;
                }))
                .then(literal("clear").executes(ctx -> {
                    if (!isEntityHighlightEnabled()) {
                        Utils.info("EntityHighlight feature is disabled.");
                        return 1;
                    }
                    EntityHighlight.clearRules();
                    Utils.info("Cleared all entity highlight rules.");
                    return 1;
                }))
                .then(literal("add")
                        .then(argument("name", StringArgumentType.word())
                                .then(argument("type", StringArgumentType.word())
                                        .suggests(EntityHighlightCommand::suggestEntityTypes)
                                        .then(argument("color", StringArgumentType.string())
                                                .suggests(CommandColorUtils::suggestColors)
                                                .executes(ctx -> {
                                                    if (!isEntityHighlightEnabled()) {
                                                        Utils.info("EntityHighlight feature is disabled.");
                                                        return 1;
                                                    }
                                                    String colorStr = StringArgumentType.getString(ctx, "color");
                                                    RenderColor color = CommandColorUtils.parseColorString(colorStr);
                                                    if (color == null) {
                                                        Utils.info("Invalid color format.");
                                                        return 1;
                                                    }
                                                    return addRuleWithColor(ctx, color);
                                                })
                                        )
                                        // RGB format
                                        .then(argument("r", IntegerArgumentType.integer(0, 255))
                                                .then(argument("g", IntegerArgumentType.integer(0, 255))
                                                        .then(argument("b", IntegerArgumentType.integer(0, 255))
                                                                .executes(ctx -> {
                                                                    if (!isEntityHighlightEnabled()) {
                                                                        Utils.info("EntityHighlight feature is disabled.");
                                                                        return 1;
                                                                    }
                                                                    int r = IntegerArgumentType.getInteger(ctx, "r");
                                                                    int g = IntegerArgumentType.getInteger(ctx, "g");
                                                                    int b = IntegerArgumentType.getInteger(ctx, "b");
                                                                    RenderColor color = new RenderColor(r, g, b, 255);
                                                                    return addRuleWithColor(ctx, color);
                                                                })
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(literal("remove")
                        .then(argument("name", StringArgumentType.word())
                                .then(argument("type", StringArgumentType.word())
                                        .executes(ctx -> {
                                            if (!isEntityHighlightEnabled()) {
                                                Utils.info("EntityHighlight feature is disabled.");
                                                return 1;
                                            }
                                            return removeRule(ctx);
                                        })
                                )
                        )
                );
    }

    private static boolean isEntityHighlightEnabled() {
        if (!FrillsConfig.instance.misc.entityHighlight.enabled.get()) {
            Utils.info("EntityHighlight feature is disabled.");
            return false;
        }
        return true;
    }

    private static int addRuleWithColor(CommandContext<FabricClientCommandSource> ctx, RenderColor color) {
        String name = StringArgumentType.getString(ctx, "name");
        String type = StringArgumentType.getString(ctx, "type");

        // Normalize "none" aliases to null
        name = normalizeNoneAlias(name);
        type = normalizeNoneAlias(type);

        // Validate and normalize name and type
        if ((name == null || name.isEmpty()) && (type == null || type.isEmpty())) {
            Utils.info("At least one of name or type must be specified (not both 'none').");
            return 1;
        }

        boolean added = EntityHighlight.addRule(name, type, color);
        String colorStr = String.format("#%06X", color.hex);

        String nameStr = (name == null || name.isEmpty()) ? "any" : name;
        String typeStr = (type == null || type.isEmpty()) ? "any" : type;

        Utils.info(
                added
                        ? "Added entity highlight rule: name=" + nameStr + ", type=" + typeStr + ", color=" + colorStr
                        : "Rule with this name and type already exists."
        );
        return 1;
    }

    private static int removeRule(CommandContext<FabricClientCommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        String type = StringArgumentType.getString(ctx, "type");

        // Normalize "none" aliases to null
        name = normalizeNoneAlias(name);
        type = normalizeNoneAlias(type);

        boolean removed = EntityHighlight.removeRule(name, type);
        String nameStr = (name == null || name.isEmpty()) ? "any" : name;
        String typeStr = (type == null || type.isEmpty()) ? "any" : type;

        Utils.info(
                removed
                        ? "Removed entity highlight rule: name=" + nameStr + ", type=" + typeStr
                        : "Rule with this name and type not found."
        );
        return 1;
    }

    private static void listRules() {
        var rules = EntityHighlight.getRules();
        if (rules.isEmpty()) {
            Utils.info("No entity highlight rules.");
            return;
        }

        StringBuilder sb = new StringBuilder("Entity highlight rules:\n");
        for (EntityHighlight.EntityHighlightRule rule : rules) {
            String name = (rule.name == null || rule.name.isEmpty()) ? "any" : rule.name;
            String type = (rule.type == null || rule.type.isEmpty()) ? "any" : rule.type;
            String colorStr = String.format("#%06X", rule.color.hex);
            sb.append("  name=").append(name).append(", type=").append(type).append(", color=").append(colorStr).append("\n");
        }
        Utils.info(sb.toString());
    }

    private static CompletableFuture<Suggestions> suggestEntityTypes(
            CommandContext<FabricClientCommandSource> ctx,
            SuggestionsBuilder builder
    ) {
        String remaining = builder.getRemaining().toLowerCase();

        Registries.ENTITY_TYPE.forEach(entityType -> {
            String id = Registries.ENTITY_TYPE.getId(entityType).toString();
            id = Utils.stripPrefix(id, "minecraft:").toLowerCase();
            if (id.startsWith(remaining)) {
                builder.suggest(id);
            }
        });

        return builder.buildFuture();
    }

    /**
     * Normalize "none" aliases to null
     * Aliases: "none", "ignore", "any", "null", ""
     */
    private static String normalizeNoneAlias(String input) {
        if (input == null) {
            return null;
        }
        String lower = input.toLowerCase();
        if (lower.equals("none") || lower.equals("ignore") || lower.equals("any") || lower.equals("null") || lower.isEmpty()) {
            return null;
        }
        return input;
    }
}

