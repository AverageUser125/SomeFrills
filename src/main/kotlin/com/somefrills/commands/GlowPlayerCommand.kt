package com.somefrills.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.somefrills.Main
import com.somefrills.features.misc.GlowPlayer
import com.somefrills.misc.RenderColor
import com.somefrills.utils.ChatUtils
import com.somefrills.utils.TextUtils
import com.somefrills.utils.isRealPlayer
import com.somefrills.utils.playerName
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import java.util.concurrent.CompletableFuture

object GlowPlayerCommand {

    fun getBuilder(): LiteralArgumentBuilder<FabricClientCommandSource> {
        return ClientCommands.literal("glowplayer")
            .executes { ctx ->
                if (!isGlowPlayerEnabled()) {
                    ChatUtils.info("GlowPlayer feature is disabled.")
                    return@executes 1
                }

                ChatUtils.info("Usage: /glowplayer <list|add|color|remove>")
                1
            }

            .then(
                ClientCommands.literal("list")
                    .executes { _ ->
                        if (!isGlowPlayerEnabled()) {
                            ChatUtils.info("GlowPlayer feature is disabled.")
                            return@executes 1
                        }

                        listGlows()
                        1
                    }
            )

            .then(
                ClientCommands.literal("add")
                    .then(
                        ClientCommands.argument("player", StringArgumentType.word())
                            .suggests(::suggestOnlinePlayers)

                            .executes { ctx ->
                                if (!isGlowPlayerEnabled()) {
                                    ChatUtils.info("GlowPlayer feature is disabled.")
                                    return@executes 1
                                }

                                addGlow(ctx, ChatFormatting.WHITE)
                            }

                            .then(
                                CommandColorUtils.buildColorArguments { ctx, color ->
                                    if (!isGlowPlayerEnabled()) {
                                        ChatUtils.info("GlowPlayer feature is disabled.")
                                        return@buildColorArguments 1
                                    }

                                    addGlowWithRenderColor(ctx, color)
                                }
                            )
                    )
            )

            .then(
                ClientCommands.literal("listglowing")
                    .executes(::listGlowingPlayers)

                    .then(
                        ClientCommands.argument("name", StringArgumentType.word())
                            .executes { ctx ->
                                listGlowingPlayers(
                                    ctx,
                                    StringArgumentType.getString(ctx, "name")
                                )
                            }
                    )
            )

            .then(
                ClientCommands.literal("color")
                    .then(
                        ClientCommands.argument("player", StringArgumentType.word())
                            .suggests(::suggestOnlinePlayers)

                            .then(
                                CommandColorUtils.buildColorArguments { ctx, color ->
                                    if (!isGlowPlayerEnabled()) {
                                        ChatUtils.info("GlowPlayer feature is disabled.")
                                        return@buildColorArguments 1
                                    }

                                    setColorWithRenderColor(ctx, color)
                                }
                            )
                    )
            )

            .then(
                ClientCommands.literal("clear")
                    .executes {
                        if (!isGlowPlayerEnabled()) {
                           ChatUtils.info("GlowPlayer feature is disabled.")
                            return@executes 1
                        }

                        GlowPlayer.clear()
                       ChatUtils.info("Cleared all forced glows.")
                        1
                    }
            )

            .then(
                ClientCommands.literal("remove")
                    .then(
                        ClientCommands.argument("player", StringArgumentType.word())
                            .suggests(::suggestGlowingPlayers)

                            .executes { ctx ->
                                if (!isGlowPlayerEnabled()) {
                                   ChatUtils.info("GlowPlayer feature is disabled.")
                                    return@executes 1
                                }

                                removeGlow(ctx)
                            }
                    )
            )
    }

    private fun isGlowPlayerEnabled(): Boolean {
        if (!GlowPlayer.isActive()) {
           ChatUtils.info("GlowPlayer feature is disabled.")
            return false
        }

        return true
    }

    /* ---------------- Command handlers ---------------- */

    private fun addGlow(
        ctx: CommandContext<FabricClientCommandSource>,
        color: ChatFormatting
    ): Int {
        val name = StringArgumentType.getString(ctx, "player")

        val renderColor = RenderColor.fromFormatting(color)

        val added = GlowPlayer.addPlayer(name, renderColor)

       ChatUtils.info(
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

        val added = GlowPlayer.addPlayer(name, color)

        val colorStr = String.format("#%06X", color.hex)

       ChatUtils.info(
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
        color: ChatFormatting
    ): Int {
        val name = StringArgumentType.getString(ctx, "player")

        val renderColor = RenderColor.fromFormatting(color)

        GlowPlayer.addPlayer(name, renderColor)

       ChatUtils.info("$name glow color set to ${color.name}.")

        applyGlowToOnlinePlayer(name)

        return 1
    }

    private fun setColorWithRenderColor(
        ctx: CommandContext<FabricClientCommandSource>,
        color: RenderColor
    ): Int {
        val name = StringArgumentType.getString(ctx, "player")

        GlowPlayer.addPlayer(name, color)

        val colorStr = String.format("#%06X", color.hex)

       ChatUtils.info("$name glow color set to $colorStr.")

        applyGlowToOnlinePlayer(name)

        return 1
    }

    private fun removeGlow(
        ctx: CommandContext<FabricClientCommandSource>
    ): Int {
        val name = StringArgumentType.getString(ctx, "player")

        val removed = GlowPlayer.removePlayer(name)

        ChatUtils.info(
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
        val names = GlowPlayer.forcedNames

        if (names.isEmpty()) {
            ChatUtils.info("No forced glows.")
            return
        }

        val sb = StringBuilder("Forced glows:\n")

        for (name in names) {
            val color = GlowPlayer.getColor(name)

            val colorStr = color?.let {
                String.format("#%06X", it.hex)
            } ?: "none"

            sb.append(name)
                .append(" (")
                .append(colorStr)
                .append(")\n")
        }

       ChatUtils.info(sb.toString())
    }

    /* ---------------- Suggestions ---------------- */

    private fun suggestOnlinePlayers(
        ctx: CommandContext<FabricClientCommandSource>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {

        val world = Main.mc.level ?: return builder.buildFuture()

        val remaining = builder.remaining.lowercase()

        for (player in world.players()) {
            if (!player.isRealPlayer()) continue
            val name = player.playerName

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

        Main.mc.level ?: return builder.buildFuture()

        val remaining = builder.remaining.lowercase()

        for (name in GlowPlayer.forcedNames) {
            if (name.lowercase().startsWith(remaining)) {
                builder.suggest(name)
            }
        }

        return builder.buildFuture()
    }

    /* ---------------- Utilities ---------------- */

    private fun applyGlowToOnlinePlayer(pureName: String) {
        val world = Main.mc.level ?: return

        for (player in world.players()) {
            if (!player.isRealPlayer()) continue

            val playerPureName = player.playerName

            if (playerPureName != pureName) continue

            val glowColor = GlowPlayer.getColor(pureName)

            if (glowColor != null) {
                GlowPlayer.setGlowImmediately(player, glowColor)
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

        val world = Main.mc.level ?: return 1

        val sb = StringBuilder("Currently glowing players:\n")

        val forcedNames = GlowPlayer.forcedNames

        for (player in world.players()) {
            if (!player.isRealPlayer()) continue
            val playerPureName = player.playerName
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
                .append(TextUtils.colorToString(GlowPlayer.getColor(playerPureName) ?: RenderColor.white))
                .append(")")
                .append(", Pos: [")
                .append(String.format("%.1f", player.x))
                .append(", ")
                .append(String.format("%.1f", player.y))
                .append(", ")
                .append(String.format("%.1f", player.z))
                .append("]\n")
        }

        ctx.source.sendFeedback(Component.literal(sb.toString()))

        return 1
    }
}