package com.somefrills.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Utils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.util.Formatting;

import java.util.concurrent.CompletableFuture;

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
}

