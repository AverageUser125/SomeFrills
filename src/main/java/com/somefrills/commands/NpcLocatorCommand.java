package com.somefrills.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.somefrills.config.FrillsConfig;
import com.somefrills.features.misc.NpcLocator;
import com.somefrills.misc.Utils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.concurrent.CompletableFuture;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class NpcLocatorCommand {

    public static LiteralArgumentBuilder<FabricClientCommandSource> getBuilder(String commandName) {
        return literal(commandName)
                .executes(ctx -> {
                    Integer checkResult = checkNpcLocatorEnabledOrWarn();
                    if (checkResult != null) return checkResult;
                    Utils.info("Usage: /npclocator <list|add|remove|clear>");
                    return 1;
                })
                .then(literal("list").executes(ctx -> {
                    Integer checkResult = checkNpcLocatorEnabledOrWarn();
                    if (checkResult != null) return checkResult;
                    return listNpcLocations();
                }))
                .then(literal("add")
                        .then(argument("npc_name", StringArgumentType.greedyString())
                                .suggests(NpcLocatorCommand::suggestAvailableNpcs)
                                .executes(ctx -> {
                                    Integer checkResult = checkNpcLocatorEnabledOrWarn();
                                    if (checkResult != null) return checkResult;
                                    return addNpcLocation(ctx);
                                })
                        )
                )
                .then(literal("remove")
                        .then(argument("npc_name", StringArgumentType.greedyString())
                                .suggests(NpcLocatorCommand::suggestTrackedNpcs)
                                .executes(ctx -> {
                                    Integer checkResult = checkNpcLocatorEnabledOrWarn();
                                    if (checkResult != null) return checkResult;
                                    return removeNpcLocation(ctx);
                                })
                        )
                )
                .then(literal("clear").executes(ctx -> {
                    Integer checkResult = checkNpcLocatorEnabledOrWarn();
                    if (checkResult != null) return checkResult;
                    return clearAllNpcLocations();
                }));
    }

    private static boolean isNpcLocatorEnabled() {
        return FrillsConfig.instance.misc.npcLocator.enabled.get();
    }

    /**
     * Checks if NPC Locator is enabled. Returns 1 (failure code) if disabled, or null if enabled.
     * Should be used like: Integer result = checkNpcLocatorEnabledOrWarn(); if (result != null) return result;
     */
    private static Integer checkNpcLocatorEnabledOrWarn() {
        if (!isNpcLocatorEnabled()) {
            Utils.info("NPC Locator feature is disabled.");
            return 1;
        }
        return null;
    }

    private static int listNpcLocations() {
        var locations = NpcLocator.getAllNpcLocations();
        if (locations.isEmpty()) {
            Utils.info("No NPCs are currently being tracked.");
        } else {
            Utils.info("Tracked NPCs:");
            locations.forEach(location -> Utils.info(Utils.format("  - {}", location.npcName())));
        }
        return 1;
    }

    private static int addNpcLocation(CommandContext<FabricClientCommandSource> ctx) {
        String npcName = StringArgumentType.getString(ctx, "npc_name");
        NpcLocator.addNpcLocation(npcName);
        return 1;
    }

    private static int removeNpcLocation(CommandContext<FabricClientCommandSource> ctx) {
        String npcName = StringArgumentType.getString(ctx, "npc_name");
        NpcLocator.removeNpcLocation(npcName);
        return 1;
    }

    private static int clearAllNpcLocations() {
        NpcLocator.clearAllNpcLocations();
        return 1;
    }


    private static CompletableFuture<Suggestions> suggestAvailableNpcs(CommandContext<FabricClientCommandSource> ctx, SuggestionsBuilder builder) {
        var availableNpcs = NpcLocator.getAvailableNpcsForCurrentIsland();
        for (String npcName : availableNpcs) {
            if (npcName.toLowerCase().startsWith(builder.getRemaining().toLowerCase())) {
                builder.suggest(npcName);
            }
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestTrackedNpcs(CommandContext<FabricClientCommandSource> ctx, SuggestionsBuilder builder) {
        var trackedNpcs = NpcLocator.getAllNpcLocations();
        for (var location : trackedNpcs) {
            if (location.npcName().toLowerCase().startsWith(builder.getRemaining().toLowerCase())) {
                builder.suggest(location.npcName());
            }
        }
        return builder.buildFuture();
    }
}
