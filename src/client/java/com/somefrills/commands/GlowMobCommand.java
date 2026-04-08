package com.somefrills.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.somefrills.config.FrillsConfig;
import com.somefrills.features.misc.GlowMob;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Utils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.registry.Registries;

import java.util.concurrent.CompletableFuture;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class GlowMobCommand {

    public static LiteralArgumentBuilder<FabricClientCommandSource> getBuilder() {
        return literal("glowmob")
                .executes(ctx -> {
                    if (!isGlowMobEnabled()) {
                        Utils.info("GlowMob feature is disabled.");
                        return 1;
                    }
                    Utils.info("Usage: /GlowMob <add|remove|list|clear>");
                    return 1;
                })
                .then(literal("list").executes(ctx -> {
                    if (!isGlowMobEnabled()) {
                        Utils.info("GlowMob feature is disabled.");
                        return 1;
                    }
                    listRules();
                    return 1;
                }))
                .then(literal("clear").executes(ctx -> {
                    if (!isGlowMobEnabled()) {
                        Utils.info("GlowMob feature is disabled.");
                        return 1;
                    }
                    GlowMob.clearRules();
                    Utils.info("Cleared all entity highlight rules.");
                    return 1;
                }))
                .then(literal("add")
                        // Path 1: /glowmob add <type> <color>
                        .then(argument("type", StringArgumentType.word())
                                .suggests(GlowMobCommand::suggestEntityTypes)
                                .then(CommandColorUtils.buildColorArguments((ctx, color) -> {
                                    if (!isGlowMobEnabled()) {
                                        Utils.info("GlowMob feature is disabled.");
                                        return 1;
                                    }
                                    String type = StringArgumentType.getString(ctx, "type");
                                    if (isValidEntityType(type)) {
                                        return addRule(null, type, color);
                                    }
                                    return 0; // Invalid type, fail silently to try other paths
                                }))
                                // Path 2: /glowmob add <type> <name> <color>
                                .then(argument("name", StringArgumentType.word())
                                        .then(CommandColorUtils.buildColorArguments((ctx, color) -> {
                                            if (!isGlowMobEnabled()) {
                                                Utils.info("GlowMob feature is disabled.");
                                                return 1;
                                            }
                                            String type = StringArgumentType.getString(ctx, "type");
                                            String name = StringArgumentType.getString(ctx, "name");

                                            // Normalize "none" aliases to null
                                            type = normalizeNoneAlias(type);
                                            name = normalizeNoneAlias(name);

                                            return addRule(name, type, color);
                                        }))
                                )
                        )
                )
                // Path 3: /glowmob add <name> <color> (for non-entity-type names)
                .then(argument("name", StringArgumentType.word())
                        .then(CommandColorUtils.buildColorArguments((ctx, color) -> {
                            if (!isGlowMobEnabled()) {
                                Utils.info("GlowMob feature is disabled.");
                                return 1;
                            }
                            String name = StringArgumentType.getString(ctx, "name");
                            if (!isValidEntityType(name)) {
                                return addRule(name, null, color);
                            }
                            return 0; // Is an entity type, so the type+color path should handle it
                        }))
                )
                .then(literal("remove")
                        .then(literal("all").executes(ctx -> {
                            if (!isGlowMobEnabled()) {
                                Utils.info("GlowMob feature is disabled.");
                                return 1;
                            }
                            GlowMob.clearRules();
                            Utils.info("Removed all entity highlight rules.");
                            return 1;
                        }))
                        .then(argument("type", StringArgumentType.word())
                                .suggests(GlowMobCommand::suggestRuleTypes)
                                .then(argument("name", StringArgumentType.word())
                                        .suggests(GlowMobCommand::suggestRuleNames)
                                        .executes(ctx -> {
                                            if (!isGlowMobEnabled()) {
                                                Utils.info("GlowMob feature is disabled.");
                                                return 1;
                                            }
                                            return removeRule(ctx);
                                        })
                                )
                        )
                );
    }

    private static boolean isGlowMobEnabled() {
        if (!FrillsConfig.instance.misc.glowMob.enabled.get()) {
            Utils.info("GlowMob feature is disabled.");
            return false;
        }
        return true;
    }

    private static boolean isValidEntityType(String value) {
        for (var entityType : Registries.ENTITY_TYPE) {
            String id = Registries.ENTITY_TYPE.getId(entityType).toString();
            id = Utils.stripPrefix(id, "minecraft:");
            if (id.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }


    private static int addRule(String name, String type, RenderColor color) {
        boolean added = GlowMob.addRule(name, type, color);
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

        boolean removed = GlowMob.removeRule(name, type);
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
        var rules = GlowMob.getRules();
        if (rules.isEmpty()) {
            Utils.info("No entity highlight rules.");
            return;
        }

        StringBuilder sb = new StringBuilder("Entity highlight rules:\n");
        for (GlowMob.GlowMobRule rule : rules) {
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
     * Suggest existing rule types for removal
     */
    private static CompletableFuture<Suggestions> suggestRuleTypes(
            CommandContext<FabricClientCommandSource> ctx,
            SuggestionsBuilder builder
    ) {
        String remaining = builder.getRemaining().toLowerCase();

        for (GlowMob.GlowMobRule rule : GlowMob.getRules()) {
            String type = (rule.type == null || rule.type.isEmpty()) ? "any" : rule.type;
            if (type.toLowerCase().startsWith(remaining)) {
                builder.suggest(type);
            }
        }

        return builder.buildFuture();
    }

    /**
     * Suggest existing rule names for removal, filtered by the selected type
     */
    private static CompletableFuture<Suggestions> suggestRuleNames(
            CommandContext<FabricClientCommandSource> ctx,
            SuggestionsBuilder builder
    ) {
        String remaining = builder.getRemaining().toLowerCase();
        String selectedType = StringArgumentType.getString(ctx, "type");
        String normalizedSelectedType = normalizeNoneAlias(selectedType);

        for (GlowMob.GlowMobRule rule : GlowMob.getRules()) {
            // Check if this rule matches the selected type
            String ruleType = (rule.type == null || rule.type.isEmpty()) ? null : rule.type;

            if ((normalizedSelectedType == null && ruleType == null) ||
                    (normalizedSelectedType != null && normalizedSelectedType.equalsIgnoreCase(ruleType))) {

                String name = (rule.name == null || rule.name.isEmpty()) ? "any" : rule.name;
                if (name.toLowerCase().startsWith(remaining)) {
                    builder.suggest(name);
                }
            }
        }

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

