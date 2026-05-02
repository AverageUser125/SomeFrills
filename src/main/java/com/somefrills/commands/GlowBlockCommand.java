package com.somefrills.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.somefrills.features.core.Features;
import com.somefrills.features.misc.glowblock.GlowBlock;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.Block;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.somefrills.Main.mc;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class GlowBlockCommand {
    public static LiteralArgumentBuilder<FabricClientCommandSource> getBuilder() {
        return LiteralArgumentBuilder.<FabricClientCommandSource>literal("glowblock")

                .executes(ctx -> {
                    Features.get(GlowBlock.class).toggle();
                    return 1;
                })

                .then(literal("add")
                        .then(argument("block", StringArgumentType.greedyString())
                                .suggests((ctx, builder) -> {
                                    if (mc.world == null || mc.world.getRegistryManager() == null) {
                                        return builder.buildFuture();
                                    }
                                    return BlockArgumentParser.getSuggestions(
                                            mc.world.getRegistryManager().getOrThrow(RegistryKeys.BLOCK),
                                            builder,
                                            false,
                                            false
                                    );
                                })
                                .executes(ctx -> {
                                    if (mc.world == null || mc.world.getRegistryManager() == null) {
                                        ctx.getSource().sendError(Text.literal("World or RegistryManager is unavailable."));
                                        return 0;
                                    }
                                    var result = BlockArgumentParser.block(
                                            mc.world.getRegistryManager().getOrThrow(RegistryKeys.BLOCK),
                                            StringArgumentType.getString(ctx, "block"),
                                            false
                                    );

                                    get().addBlock(result.blockState().getBlock());
                                    return 1;
                                }))
                )
                .then(literal("clear"))
                .executes(ctx -> {
                    get().clear();
                    ctx.getSource().sendFeedback(Text.literal("Cleared all glow blocks."));
                    return 1;
                })
                .then(literal("remove")
                        .then(argument("block", StringArgumentType.word())
                                .suggests(GlowBlockCommand::suggestTrackedBlocks)
                                .executes(ctx -> {
                                    if (mc.world == null || mc.world.getRegistryManager() == null) {
                                        ctx.getSource().sendError(Text.literal("World or RegistryManager is unavailable."));
                                        return 0;
                                    }
                                    var result = BlockArgumentParser.block(
                                            mc.world.getRegistryManager().getOrThrow(RegistryKeys.BLOCK),
                                            StringArgumentType.getString(ctx, "block"),
                                            false
                                    );

                                    get().removeBlock(result.blockState().getBlock());
                                    ctx.getSource().sendFeedback(Text.literal("Removed glow block."));
                                    return 1;
                                }))
                )

                .then(literal("list")
                        .executes(ctx -> {
                            GlowBlock glowBlock = get();

                            if (glowBlock.getTargetBlocks().isEmpty()) {
                                ctx.getSource().sendFeedback(Text.literal("No tracked glow blocks."));
                                return 1;
                            }

                            String blocks = glowBlock.getTargetBlocks().stream()
                                    .map(block -> Registries.BLOCK.getId(block).toString())
                                    .collect(Collectors.joining(", "));

                            ctx.getSource().sendFeedback(
                                    Text.literal("Tracked glow blocks: " + blocks)
                            );

                            return 1;
                        })
                );
    }

    private static CompletableFuture<Suggestions> suggestTrackedBlocks(
            com.mojang.brigadier.context.CommandContext<FabricClientCommandSource> ctx,
            SuggestionsBuilder builder
    ) {
        for (Block block : get().getTargetBlocks()) {
            builder.suggest(Registries.BLOCK.getId(block).toString());
        }

        return builder.buildFuture();
    }

    private static GlowBlock get() {
        return Features.get(GlowBlock.class);
    }
}