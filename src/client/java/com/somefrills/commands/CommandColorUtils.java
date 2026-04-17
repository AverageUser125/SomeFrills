package com.somefrills.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Utils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.util.Formatting;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

/**
 * Shared color parsing and suggestion utilities for commands
 */
public class CommandColorUtils {
    /**
     * Suggest available colors for command completion
     */
    public static CompletableFuture<Suggestions> suggestColors(
            CommandContext<FabricClientCommandSource> ctx,
            SuggestionsBuilder builder
    ) {
        String remaining = builder.getRemaining().toLowerCase();
        for (Formatting f : Formatting.values()) {
            if (f.isColor() && f.getName().startsWith(remaining)) {
                builder.suggest(f.getName());
            }
        }
        return builder.buildFuture();
    }

    /**
     * Build a color argument that stores the color for later retrieval
     * Does NOT execute, just parses and stores the color argument
     */
    public static RequiredArgumentBuilder<FabricClientCommandSource, String> buildColorArgument() {
        return argument("color", StringArgumentType.string())
                .suggests(CommandColorUtils::suggestColors);
    }

    /**
     * Extract the color from context
     */
    public static RenderColor getColorArgument(CommandContext<FabricClientCommandSource> ctx) {
        String colorStr = StringArgumentType.getString(ctx, "color");
        Optional<RenderColor> color = Utils.parseRenderColor(colorStr);
        if (color.isEmpty()) {
            throw new IllegalArgumentException("Invalid color format: " + colorStr);
        }
        return color.get();
    }

    /**
     * Build complete color argument chain (string color only)
     * Supports string input (formatting name/hex)
     * Handler receives: context, color
     */
    public static RequiredArgumentBuilder<FabricClientCommandSource, String> buildColorArguments(
            BiFunction<CommandContext<FabricClientCommandSource>, RenderColor, Integer> handler
    ) {
        return argument("color", StringArgumentType.string())
                .suggests(CommandColorUtils::suggestColors)
                .executes(ctx -> {
                    String colorStr = StringArgumentType.getString(ctx, "color");
                    Optional<RenderColor> color = Utils.parseRenderColor(colorStr);
                    if (color.isEmpty()) {
                        Utils.info("Invalid color format.");
                        return 1;
                    }
                    return handler.apply(ctx, color.get());
                });
    }
}
