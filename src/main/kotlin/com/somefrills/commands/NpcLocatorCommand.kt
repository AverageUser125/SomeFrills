package com.somefrills.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.somefrills.features.core.Features
import com.somefrills.features.misc.NpcLocator
import com.somefrills.misc.Utils
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import java.util.concurrent.CompletableFuture

object NpcLocatorCommand {

    fun getBuilder(
        commandName: String
    ): LiteralArgumentBuilder<FabricClientCommandSource> {

        return literal(commandName)

            .executes {

                val checkResult =
                    checkNpcLocatorEnabledOrWarn()

                if (checkResult != null) {
                    return@executes checkResult
                }

                Utils.info(
                    "Usage: /npclocator <list|add|remove|clear>"
                )

                1
            }

            .then(
                literal("list")

                    .executes {

                        val checkResult =
                            checkNpcLocatorEnabledOrWarn()

                        if (checkResult != null) {
                            return@executes checkResult
                        }

                        listNpcLocations()
                    }
            )

            .then(
                literal("add")

                    .then(
                        argument(
                            "npc_name",
                            StringArgumentType.greedyString()
                        )

                            .suggests(::suggestAvailableNpcs)

                            .executes { ctx ->

                                val checkResult =
                                    checkNpcLocatorEnabledOrWarn()

                                if (checkResult != null) {
                                    return@executes checkResult
                                }

                                addNpcLocation(ctx)
                            }
                    )
            )

            .then(
                literal("remove")

                    .then(
                        argument(
                            "npc_name",
                            StringArgumentType.greedyString()
                        )

                            .suggests(::suggestTrackedNpcs)

                            .executes { ctx ->

                                val checkResult =
                                    checkNpcLocatorEnabledOrWarn()

                                if (checkResult != null) {
                                    return@executes checkResult
                                }

                                removeNpcLocation(ctx)
                            }
                    )
            )

            .then(
                literal("clear")

                    .executes {

                        val checkResult =
                            checkNpcLocatorEnabledOrWarn()

                        if (checkResult != null) {
                            return@executes checkResult
                        }

                        clearAllNpcLocations()
                    }
            )
    }

    private fun isNpcLocatorEnabled(): Boolean {
        return Features.isActive(NpcLocator::class.java)
    }

    /**
     * Returns:
     * - null if enabled
     * - 1 if disabled
     */
    private fun checkNpcLocatorEnabledOrWarn(): Int? {

        if (!isNpcLocatorEnabled()) {

            Utils.info(
                "NPC Locator feature is disabled."
            )

            return 1
        }

        return null
    }

    private fun listNpcLocations(): Int {

        val locations =
            NpcLocator.getAllNpcLocations()

        if (locations.isEmpty()) {

            Utils.info(
                "No NPCs are currently being tracked."
            )

        } else {

            Utils.info("Tracked NPCs:")

            locations.forEach { location ->

                Utils.info(
                    Utils.format(
                        "  - {}",
                        location.npcName
                    )
                )
            }
        }

        return 1
    }

    private fun addNpcLocation(
        ctx: CommandContext<FabricClientCommandSource>
    ): Int {

        val npcName =
            StringArgumentType.getString(
                ctx,
                "npc_name"
            )

        NpcLocator.addNpcLocation(npcName)

        return 1
    }

    private fun removeNpcLocation(
        ctx: CommandContext<FabricClientCommandSource>
    ): Int {

        val npcName =
            StringArgumentType.getString(
                ctx,
                "npc_name"
            )

        NpcLocator.removeNpcLocation(npcName)

        return 1
    }

    private fun clearAllNpcLocations(): Int {

        NpcLocator.clearAllNpcLocations()

        return 1
    }

    private fun suggestAvailableNpcs(
        ctx: CommandContext<FabricClientCommandSource>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {

        val remaining =
            builder.remaining.lowercase()

        val availableNpcs =
            NpcLocator.getAvailableNpcsForCurrentIsland()

        for (npcName in availableNpcs) {

            if (
                npcName.lowercase()
                    .startsWith(remaining)
            ) {

                builder.suggest(npcName)
            }
        }

        return builder.buildFuture()
    }

    private fun suggestTrackedNpcs(
        ctx: CommandContext<FabricClientCommandSource>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {

        val remaining =
            builder.remaining.lowercase()

        val trackedNpcs =
            NpcLocator.getAllNpcLocations()

        for (location in trackedNpcs) {

            if (
                location.npcName.lowercase()
                    .startsWith(remaining)
            ) {

                builder.suggest(location.npcName)
            }
        }

        return builder.buildFuture()
    }
}