package com.somefrills.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Utils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.util.Formatting;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

/**
 * Shared color parsing and suggestion utilities for commands
 */
public class CommandColorUtils {

    /**
     * Parse a color string which can be:
     * - A formatting color name (e.g., "red", "white")
     * - A 6-char hex color (e.g., "#FFFFFF", "FFFFFF") - RGB format
     */
    public static RenderColor parseColorString(String colorStr) {
        if (colorStr == null) return null;

        // Try hex format first (#FFFFFF or FFFFFF)
        if (colorStr.startsWith("#")) {
            colorStr = colorStr.substring(1);
        }

        if (colorStr.length() == 6) {
            try {
                int hex = Integer.parseInt(colorStr, 16);
                return RenderColor.fromHex(hex);
            } catch (NumberFormatException e) {
                // Fall through to formatting check
            }
        }

        // Try formatting color
        Formatting formatting = Utils.parseColor(colorStr);
        if (formatting != null) {
            return RenderColor.fromFormatting(formatting);
        }

        return null;
    }

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
     * Build RGB argument chain (r, g, b) that executes a handler function with the resulting color
     * Handler receives: context, color
     */
    public static RequiredArgumentBuilder<FabricClientCommandSource, Integer> buildRGBArguments(
            BiFunction<CommandContext<FabricClientCommandSource>, RenderColor, Integer> handler
    ) {
        return argument("r", IntegerArgumentType.integer(0, 255))
                .then(argument("g", IntegerArgumentType.integer(0, 255))
                        .then(argument("b", IntegerArgumentType.integer(0, 255))
                                .executes(ctx -> {
                                    int r = IntegerArgumentType.getInteger(ctx, "r");
                                    int g = IntegerArgumentType.getInteger(ctx, "g");
                                    int b = IntegerArgumentType.getInteger(ctx, "b");
                                    RenderColor color = new RenderColor(r, g, b, 255);
                                    return handler.apply(ctx, color);
                                })
                        )
                );
    }

    /**
     * Build complete color argument chain (string color OR RGB)
     * Supports both string input (formatting name/hex) and RGB input
     * Handler receives: context, color
     */
    public static RequiredArgumentBuilder<FabricClientCommandSource, String> buildColorArguments(
            BiFunction<CommandContext<FabricClientCommandSource>, RenderColor, Integer> handler
    ) {
        return argument("color", StringArgumentType.string())
                .suggests(CommandColorUtils::suggestColors)
                .executes(ctx -> {
                    String colorStr = StringArgumentType.getString(ctx, "color");
                    RenderColor color = parseColorString(colorStr);
                    if (color == null) {
                        Utils.info("Invalid color format.");
                        return 1;
                    }
                    return handler.apply(ctx, color);
                })
                // Also support RGB format
                .then(buildRGBArguments(handler));
    }
}
