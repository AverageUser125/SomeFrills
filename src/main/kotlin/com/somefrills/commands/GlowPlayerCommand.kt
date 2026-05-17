package com.somefrills.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.somefrills.Main
import com.somefrills.features.core.Features
import com.somefrills.features.misc.GlowPlayer
import com.somefrills.misc.RenderColor
import com.somefrills.misc.Utils
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.concurrent.CompletableFuture

object GlowPlayerCommand {

    fun getBuilder(): LiteralArgumentBuilder<FabricClientCommandSource> {
        return ClientCommandManager.literal("glowplayer")
            .executes { ctx ->
                if (!isGlowPlayerEnabled()) {
                    Utils.info("GlowPlayer feature is disabled.")
                    return@executes 1
                }

                Utils.info("Usage: /glowplayer <list|add|color|remove>")
                1
            }

            .then(
                ClientCommandManager.literal("list")
                    .executes { _ ->
                        if (!isGlowPlayerEnabled()) {
                            Utils.info("GlowPlayer feature is disabled.")
                            return@executes 1
                        }

                        listGlows()
                        1
                    }
            )

            .then(
                ClientCommandManager.literal("add")
                    .then(
                        ClientCommandManager.argument("player", StringArgumentType.word())
                            .suggests(::suggestOnlinePlayers)

                            .executes { ctx ->
                                if (!isGlowPlayerEnabled()) {
                                    Utils.info("GlowPlayer feature is disabled.")
                                    return@executes 1
                                }

                                addGlow(ctx, Formatting.WHITE)
                            }

                            .then(
                                CommandColorUtils.buildColorArguments { ctx, color ->
                                    if (!isGlowPlayerEnabled()) {
                                        Utils.info("GlowPlayer feature is disabled.")
                                        return@buildColorArguments 1
                                    }

                                    addGlowWithRenderColor(ctx, color)
                                }
                            )
                    )
            )

            .then(
                ClientCommandManager.literal("listglowing")
                    .executes(::listGlowingPlayers)

                    .then(
                        ClientCommandManager.argument("name", StringArgumentType.word())
                            .executes { ctx ->
                                listGlowingPlayers(
                                    ctx,
                                    StringArgumentType.getString(ctx, "name")
                                )
                            }
                    )
            )

            .then(
                ClientCommandManager.literal("color")
                    .then(
                        ClientCommandManager.argument("player", StringArgumentType.word())
                            .suggests(::suggestOnlinePlayers)

                            .then(
                                CommandColorUtils.buildColorArguments { ctx, color ->
                                    if (!isGlowPlayerEnabled()) {
                                        Utils.info("GlowPlayer feature is disabled.")
                                        return@buildColorArguments 1
                                    }

                                    setColorWithRenderColor(ctx, color)
                                }
                            )
                    )
            )

            .then(
                ClientCommandManager.literal("clear")
                    .executes {
                        if (!isGlowPlayerEnabled()) {
                            Utils.info("GlowPlayer feature is disabled.")
                            return@executes 1
                        }

                        get().clear()
                        Utils.info("Cleared all forced glows.")
                        1
                    }
            )

            .then(
                ClientCommandManager.literal("remove")
                    .then(
                        ClientCommandManager.argument("player", StringArgumentType.word())
                            .suggests(::suggestGlowingPlayers)

                            .executes { ctx ->
                                if (!isGlowPlayerEnabled()) {
                                    Utils.info("GlowPlayer feature is disabled.")
                                    return@executes 1
                                }

                                removeGlow(ctx)
                            }
                    )
            )
    }

    private fun isGlowPlayerEnabled(): Boolean {
        if (!Features.isActive(GlowPlayer::class.java)) {
            Utils.info("GlowPlayer feature is disabled.")
            return false
        }

        return true
    }

    /* ---------------- Command handlers ---------------- */

    private fun addGlow(
        ctx: CommandContext<FabricClientCommandSource>,
        color: Formatting
    ): Int {
        val name = StringArgumentType.getString(ctx, "player")

        val renderColor = RenderColor.fromFormatting(color)

        val added = get().addPlayer(name, renderColor)

        Utils.info(
            if (added) {
                "$name will now glow (${color.name})."
            } else {
                "$name is already glowing."
            }
        )

        applyGlowToOnlinePlayer(name)

        return 1
    }

    private fun addGlowWithRenderColor(
        ctx: CommandContext<FabricClientCommandSource>,
        color: RenderColor
    ): Int {
        val name = StringArgumentType.getString(ctx, "player")

        val added = get().addPlayer(name, color)

        val colorStr = String.format("#%06X", color.hex)

        Utils.info(
            if (added) {
                "$name will now glow ($colorStr)."
            } else {
                "$name is already glowing."
            }
        )

        applyGlowToOnlinePlayer(name)

        return 1
    }

    private fun setColor(
        ctx: CommandContext<FabricClientCommandSource>,
        color: Formatting
    ): Int {
        val name = StringArgumentType.getString(ctx, "player")

        val renderColor = RenderColor.fromFormatting(color)

        get().addPlayer(name, renderColor)

        Utils.info("$name glow color set to ${color.name}.")

        applyGlowToOnlinePlayer(name)

        return 1
    }

    private fun setColorWithRenderColor(
        ctx: CommandContext<FabricClientCommandSource>,
        color: RenderColor
    ): Int {
        val name = StringArgumentType.getString(ctx, "player")

        get().addPlayer(name, color)

        val colorStr = String.format("#%06X", color.hex)

        Utils.info("$name glow color set to $colorStr.")

        applyGlowToOnlinePlayer(name)

        return 1
    }

    private fun removeGlow(
        ctx: CommandContext<FabricClientCommandSource>
    ): Int {
        val name = StringArgumentType.getString(ctx, "player")

        val removed = get().removePlayer(name)

        Utils.info(
            if (removed) {
                "$name will no longer glow."
            } else {
                "$name was not glowing."
            }
        )

        return 1
    }

    /* ---------------- Listing ---------------- */

    private fun listGlows() {
        val names = get().forcedNames

        if (names.isEmpty()) {
            Utils.info("No forced glows.")
            return
        }

        val sb = StringBuilder("Forced glows:\n")

        for (name in names) {
            val color = get().getColor(name)

            val colorStr = color?.let {
                String.format("#%06X", it.hex)
            } ?: "none"

            sb.append(name)
                .append(" (")
                .append(colorStr)
                .append(")\n")
        }

        Utils.info(sb.toString())
    }

    /* ---------------- Suggestions ---------------- */

    private fun suggestOnlinePlayers(
        ctx: CommandContext<FabricClientCommandSource>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {

        val world = Main.mc.world ?: return builder.buildFuture()

        val remaining = builder.remaining.lowercase()

        for (player in world.players) {
            if (!Utils.isRealPlayer(player)) continue

            val name = player.name.string

            if (name.lowercase().startsWith(remaining)) {
                builder.suggest(name)
            }
        }

        return builder.buildFuture()
    }

    private fun suggestGlowingPlayers(
        ctx: CommandContext<FabricClientCommandSource>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {

        Main.mc.world ?: return builder.buildFuture()

        val remaining = builder.remaining.lowercase()

        for (name in get().forcedNames) {
            if (name.lowercase().startsWith(remaining)) {
                builder.suggest(name)
            }
        }

        return builder.buildFuture()
    }

    /* ---------------- Utilities ---------------- */

    private fun applyGlowToOnlinePlayer(pureName: String) {
        val world = Main.mc.world ?: return

        for (player in world.players) {
            if (!Utils.isRealPlayer(player)) continue

            val playerPureName = Utils.getPlayerName(player)

            if (playerPureName != pureName) continue

            val glowColor = get().getColor(pureName)

            if (glowColor != null) {
                get().setGlowImmediately(player, glowColor)
            }
        }
    }

    fun listGlowingPlayers(
        ctx: CommandContext<FabricClientCommandSource>
    ): Int {
        return listGlowingPlayers(ctx, "")
    }

    fun listGlowingPlayers(
        ctx: CommandContext<FabricClientCommandSource>,
        name: String
    ): Int {

        val world = Main.mc.world ?: return 1

        val sb = StringBuilder("Currently glowing players:\n")

        val forcedNames = get().forcedNames

        for (player in world.players) {
            if (!Utils.isRealPlayer(player)) continue

            val playerPureName = Utils.getPlayerName(player) ?: continue

            if (
                name.isNotEmpty() &&
                !playerPureName.lowercase().contains(name.lowercase())
            ) {
                continue
            }

            if (!forcedNames.contains(playerPureName)) continue

            sb.append("  - ")
                .append(playerPureName)
                .append("(")
                .append(Utils.colorToString(get().getColor(playerPureName)))
                .append(")")
                .append(", Pos: [")
                .append(String.format("%.1f", player.x))
                .append(", ")
                .append(String.format("%.1f", player.y))
                .append(", ")
                .append(String.format("%.1f", player.z))
                .append("]\n")
        }

        ctx.source.sendFeedback(Text.literal(sb.toString()))

        return 1
    }

    fun get(): GlowPlayer {
        return Features.get(GlowPlayer::class.java)
    }
}