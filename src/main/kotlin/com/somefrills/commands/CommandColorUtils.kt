package com.somefrills.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.somefrills.misc.RenderColor
import com.somefrills.utils.ChatUtils
import com.somefrills.utils.TextUtils
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.util.Formatting
import java.util.concurrent.CompletableFuture

/**
 * Shared color parsing and suggestion utilities for commands
 */
object CommandColorUtils {

    /**
     * Suggest available colors for command completion
     */
    fun suggestColors(
        ctx: CommandContext<FabricClientCommandSource>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {

        val remaining = builder.remaining.lowercase()

        for (formatting in Formatting.entries) {

            if (
                formatting.isColor &&
                formatting.name.lowercase().startsWith(remaining)
            ) {

                builder.suggest(
                    formatting.name.lowercase()
                )
            }
        }

        return builder.buildFuture()
    }

    /**
     * Build a color argument that stores the color for later retrieval
     * Does NOT execute, just parses and stores the color argument
     */
    fun buildColorArgument():
            RequiredArgumentBuilder<FabricClientCommandSource, String> {

        return argument(
            "color",
            StringArgumentType.string()
        ).suggests(::suggestColors)
    }

    /**
     * Extract the color from context
     */
    fun getColorArgument(
        ctx: CommandContext<FabricClientCommandSource>
    ): RenderColor {

        val colorStr =
            StringArgumentType.getString(ctx, "color")

        val color =
            TextUtils.parseRenderColor(colorStr) ?: throw IllegalArgumentException(
                "Invalid color format: $colorStr"
            )

        return color
    }

    /**
     * Build complete color argument chain
     * Handler receives: context, color
     */
    fun buildColorArguments(
        handler: (
            CommandContext<FabricClientCommandSource>,
            RenderColor
        ) -> Int
    ): RequiredArgumentBuilder<FabricClientCommandSource, String> {

        return argument(
            "color",
            StringArgumentType.string()
        )

            .suggests(::suggestColors)

            .executes { ctx ->

                val colorStr =
                    StringArgumentType.getString(ctx, "color")

                val color =
                    TextUtils.parseRenderColor(colorStr)

                if (color == null) {

                    ChatUtils.info(
                        "Invalid color format."
                    )

                    return@executes 1
                }

                handler(
                    ctx,
                    color
                )
            }
    }
}