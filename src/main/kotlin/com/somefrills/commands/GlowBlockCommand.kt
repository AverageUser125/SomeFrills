package com.somefrills.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.somefrills.Main.mc
import com.somefrills.features.core.Features
import com.somefrills.features.misc.glowblock.GlowBlock
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.block.Block
import net.minecraft.command.argument.BlockArgumentParser
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKeys
import net.minecraft.text.Text
import java.util.concurrent.CompletableFuture

object GlowBlockCommand {

    fun getBuilder(): LiteralArgumentBuilder<FabricClientCommandSource> {
        return literal("glowblock")

            .executes {
                get().toggle()
                1
            }

            .then(
                literal("add")
                    .then(
                        argument("block", StringArgumentType.greedyString())

                            .suggests { _, builder ->

                                val world = mc.world

                                if (world == null || world.registryManager == null) {
                                    return@suggests builder.buildFuture()
                                }

                                BlockArgumentParser.getSuggestions(
                                    world.registryManager.getOrThrow(RegistryKeys.BLOCK),
                                    builder,
                                    false,
                                    false
                                )
                            }

                            .executes { ctx ->

                                val world = mc.world

                                if (world == null || world.registryManager == null) {
                                    ctx.source.sendError(
                                        Text.literal("World or RegistryManager is unavailable.")
                                    )
                                    return@executes 0
                                }

                                val result = BlockArgumentParser.block(
                                    world.registryManager.getOrThrow(RegistryKeys.BLOCK),
                                    StringArgumentType.getString(ctx, "block"),
                                    false
                                )

                                get().addBlock(result.blockState().block)

                                1
                            }
                    )
            )

            .then(
                literal("clear")
                    .executes { ctx ->

                        get().clear()

                        ctx.source.sendFeedback(
                            Text.literal("Cleared all glow blocks.")
                        )

                        1
                    }
            )

            .then(
                literal("remove")
                    .then(
                        argument("block", StringArgumentType.word())
                            .suggests(::suggestTrackedBlocks)

                            .executes { ctx ->

                                val world = mc.world

                                if (world == null || world.registryManager == null) {
                                    ctx.source.sendError(
                                        Text.literal("World or RegistryManager is unavailable.")
                                    )

                                    return@executes 0
                                }

                                val result = BlockArgumentParser.block(
                                    world.registryManager.getOrThrow(RegistryKeys.BLOCK),
                                    StringArgumentType.getString(ctx, "block"),
                                    false
                                )

                                get().removeBlock(result.blockState().block)

                                ctx.source.sendFeedback(
                                    Text.literal("Removed glow block.")
                                )

                                1
                            }
                    )
            )

            .then(
                literal("list")
                    .executes { ctx ->
                        val glowBlock = get()

                        if (glowBlock.targetBlocks.isEmpty()) {
                            ctx.source.sendFeedback(
                                Text.literal("No tracked glow blocks.")
                            )

                            return@executes 1
                        }

                        val blocks = glowBlock.targetBlocks
                            .joinToString(", ") {
                                Registries.BLOCK.getId(it).toString()
                            }

                        ctx.source.sendFeedback(
                            Text.literal("Tracked glow blocks: $blocks")
                        )

                        1
                    }
            )
    }

    private fun suggestTrackedBlocks(
        ctx: CommandContext<FabricClientCommandSource>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {

        for (block in get().targetBlocks) {
            builder.suggest(
                Registries.BLOCK.getId(block).toString()
            )
        }

        return builder.buildFuture()
    }

    private fun get(): GlowBlock {
        return Features.get(GlowBlock::class.java)
    }
}