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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

/**
 * Shared color parsing and suggestion utilities for commands
 */
public class CommandColorUtils {

    /**
     * Parse a color string with Optional return and support for RGB format.
     * Supports:
     * - Formatting color names (e.g., "red", "white")
     * - Hex colors (e.g., "#FFFFFF", "FFFFFF")
     * - RGB values (e.g., "255 0 0" or space-separated 0-255 values)
     *
     * @param colorStr the color string to parse
     * @return Optional containing the parsed RenderColor, or empty if parsing fails
     */
    public static Optional<RenderColor> parseColor(String colorStr) {
        if (colorStr == null || colorStr.isEmpty()) {
            return Optional.empty();
        }

        // Try hex format
        String hexStr = colorStr;
        if (hexStr.startsWith("#")) {
            hexStr = hexStr.substring(1);
        }
        if (hexStr.length() == 6) {
            try {
                int hex = Integer.parseInt(hexStr, 16);
                return Optional.of(RenderColor.fromHex(hex));
            } catch (NumberFormatException ignored) {
            }
        }

        // Try formatting color
        Formatting formatting = Utils.parseColor(colorStr);
        if (formatting != null) {
            return Optional.of(RenderColor.fromFormatting(formatting));
        }

        // Try RGB format (space-separated)
        String[] parts = colorStr.split("\\s+");
        if (parts.length == 3) {
            try {
                int r = Integer.parseInt(parts[0]);
                int g = Integer.parseInt(parts[1]);
                int b = Integer.parseInt(parts[2]);
                if (r >= 0 && r <= 255 && g >= 0 && g <= 255 && b >= 0 && b <= 255) {
                    return Optional.of(new RenderColor(r, g, b, 255));
                }
            } catch (NumberFormatException ignored) {
            }
        }

        return Optional.empty();
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
                    Optional<RenderColor> color = parseColor(colorStr);
                    if (color.isEmpty()) {
                        Utils.info("Invalid color format.");
                        return 1;
                    }
                    return handler.apply(ctx, color.get());
                })
                // Also support RGB format
                .then(buildRGBArguments(handler));
    }
}
