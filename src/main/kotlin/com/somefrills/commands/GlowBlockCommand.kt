package com.somefrills.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.somefrills.Main.mc
import com.somefrills.features.misc.glowblock.GlowBlock
import net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument
import net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.commands.arguments.blocks.BlockStateParser
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import java.util.concurrent.CompletableFuture

object GlowBlockCommand {

    fun getBuilder(): LiteralArgumentBuilder<FabricClientCommandSource> {
        return literal("glowblock")

            .executes {
                GlowBlock.toggle()
                1
            }

            .then(
                literal("add")
                    .then(
                        argument("block", StringArgumentType.greedyString())

                            .suggests { _, builder ->

                                val world = mc.level

                                if (world == null) {
                                    return@suggests builder.buildFuture()
                                }
                                BlockStateParser.fillSuggestions(
                                    world.registryAccess().lookupOrThrow(Registries.BLOCK),
                                    builder,
                                    false,
                                    false
                                )
                            }

                            .executes { ctx ->

                                val world = mc.level

                                if (world == null) {
                                    ctx.source.sendError(
                                        Component.literal("World or RegistryManager is unavailable.")
                                    )
                                    return@executes 0
                                }

                                val result = BlockStateParser.parseForBlock(
                                    world.registryAccess().lookupOrThrow(Registries.BLOCK),
                                    StringArgumentType.getString(ctx, "block"),
                                    false
                                )

                                GlowBlock.addBlock(result.blockState().block)

                                1
                            }
                    )
            )

            .then(
                literal("clear")
                    .executes { ctx ->

                        GlowBlock.clear()

                        ctx.source.sendFeedback(
                            Component.literal("Cleared all glow blocks.")
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

                                val world = mc.level

                                if (world == null) {
                                    ctx.source.sendError(
                                        Component.literal("World or RegistryManager is unavailable.")
                                    )

                                    return@executes 0
                                }

                                val result = BlockStateParser.parseForBlock(
                                    world.registryAccess().lookupOrThrow(Registries.BLOCK),
                                    StringArgumentType.getString(ctx, "block"),
                                    false
                                )

                                GlowBlock.removeBlock(result.blockState().block)

                                ctx.source.sendFeedback(
                                    Component.literal("Removed glow block.")
                                )

                                1
                            }
                    )
            )

            .then(
                literal("list")
                    .executes { ctx ->
                        if (GlowBlock.targetBlocks.isEmpty()) {
                            ctx.source.sendFeedback(
                                Component.literal("No tracked glow blocks.")
                            )

                            return@executes 1
                        }

                        val sb = StringBuilder()
                        for (block in GlowBlock.targetBlocks) {
                            sb.append(
                                BuiltInRegistries.BLOCK.getKey(block).toString()
                            ).append(", ")
                        }
                        sb.setLength(sb.length - 2) // Remove last comma and space

                        ctx.source.sendFeedback(
                            Component.literal("Tracked glow blocks: $sb")
                        )

                        1
                    }
            )
    }

    private fun suggestTrackedBlocks(
        ctx: CommandContext<FabricClientCommandSource>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {

        for (block in GlowBlock.targetBlocks) {
            builder.suggest(
                BuiltInRegistries.BLOCK.getKey(block).toString()
            )
        }

        return builder.buildFuture()
    }
}