package com.somefrills.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.somefrills.config.FrillsConfig;
import com.somefrills.features.misc.GlowPlayer;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Utils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Formatting;

import java.util.concurrent.CompletableFuture;

import static com.somefrills.Main.mc;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class GlowPlayerCommand {

    // No constructor: this class only provides the command builder. Tick and connection handling
    // are handled by the event-driven `GlowPlayer` feature class.

    public static LiteralArgumentBuilder<FabricClientCommandSource> getBuilder() {
        return literal("glowplayer")
                .executes(ctx -> {
                    if (!isGlowPlayerEnabled()) {
                        Utils.info("GlowPlayer feature is disabled.");
                        return 1;
                    }
                    Utils.info("Usage: /glowplayer <list|add|color|remove>");
                    return 1;
                })
                .then(literal("list").executes(ctx -> {
                    if (!isGlowPlayerEnabled()) {
                        Utils.info("GlowPlayer feature is disabled.");
                        return 1;
                    }
                    listGlows();
                    return 1;
                }))
                .then(literal("add")
                        .then(argument("player", StringArgumentType.word())
                                .suggests(GlowPlayerCommand::suggestOnlinePlayers)
                                .executes(ctx -> {
                                    if (!isGlowPlayerEnabled()) {
                                        Utils.info("GlowPlayer feature is disabled.");
                                        return 1;
                                    }
                                    return addGlow(ctx, Formatting.WHITE);
                                })
                                // String color format (formatting name or hex) or RGB format
                                .then(CommandColorUtils.buildColorArguments((ctx, color) -> {
                                    if (!isGlowPlayerEnabled()) {
                                        Utils.info("GlowPlayer feature is disabled.");
                                        return 1;
                                    }
                                    return addGlowWithRenderColor(ctx, color);
                                }))
                        )
                )
                .then(literal("color")
                        .then(argument("player", StringArgumentType.word())
                                .suggests(GlowPlayerCommand::suggestOnlinePlayers)
                                .then(CommandColorUtils.buildColorArguments((ctx, color) -> {
                                    if (!isGlowPlayerEnabled()) {
                                        Utils.info("GlowPlayer feature is disabled.");
                                        return 1;
                                    }
                                    return setColorWithRenderColor(ctx, color);
                                }))
                        )
                )
                .then(literal("remove")
                        .then(argument("player", StringArgumentType.word())
                                .suggests(GlowPlayerCommand::suggestOnlinePlayers)
                                .executes(ctx -> {
                                    if (!isGlowPlayerEnabled()) {
                                        Utils.info("GlowPlayer feature is disabled.");
                                        return 1;
                                    }
                                    return removeGlow(ctx);
                                })
                        )
                        .then(literal("all").executes(ctx -> {
                            if (!isGlowPlayerEnabled()) {
                                return 1;
                            }
                            GlowPlayer.clear();
                            Utils.info("Cleared all forced glows.");
                            return 1;
                        }))
                );
    }

    private static boolean isGlowPlayerEnabled() {
        if (!FrillsConfig.instance.misc.glowPlayer.enabled.get()) {
            Utils.info("GlowPlayer feature is disabled.");
            return false;
        }
        return true;
    }
    /* ---------------- Command handlers ---------------- */

    private static int addGlow(CommandContext<FabricClientCommandSource> ctx, Formatting color) {
        String rawName = StringArgumentType.getString(ctx, "player");
        String pureName = GlowPlayer.convertToPureName(rawName);

        if (pureName == null) {
            Utils.info("Invalid player name.");
            return 1;
        }

        RenderColor renderColor = RenderColor.fromFormatting(color);
        boolean added = GlowPlayer.addPlayer(pureName, renderColor);
        Utils.info(
                added
                        ? pureName + " will now glow (" + color.getName() + ")."
                        : pureName + " is already glowing."
        );
        applyGlowToOnlinePlayer(pureName);
        return 1;
    }

    private static int addGlowWithRenderColor(CommandContext<FabricClientCommandSource> ctx, RenderColor color) {
        String rawName = StringArgumentType.getString(ctx, "player");
        String pureName = GlowPlayer.convertToPureName(rawName);

        if (pureName == null) {
            Utils.info("Invalid player name.");
            return 1;
        }

        boolean added = GlowPlayer.addPlayer(pureName, color);
        String colorStr = String.format("#%06X", color.hex);
        Utils.info(
                added
                        ? pureName + " will now glow (" + colorStr + ")."
                        : pureName + " is already glowing."
        );
        applyGlowToOnlinePlayer(pureName);
        return 1;
    }

    private static int setColor(CommandContext<FabricClientCommandSource> ctx, Formatting color) {
        String rawName = StringArgumentType.getString(ctx, "player");
        String pureName = GlowPlayer.convertToPureName(rawName);

        if (pureName == null) {
            Utils.info("Invalid player name.");
            return 1;
        }

        // Set color even if the player wasn't previously added
        RenderColor renderColor = RenderColor.fromFormatting(color);
        GlowPlayer.addPlayer(pureName, renderColor);
        Utils.info(pureName + " glow color set to " + color.getName() + ".");
        applyGlowToOnlinePlayer(pureName);
        return 1;
    }

    private static int setColorWithRenderColor(CommandContext<FabricClientCommandSource> ctx, RenderColor color) {
        String rawName = StringArgumentType.getString(ctx, "player");
        String pureName = GlowPlayer.convertToPureName(rawName);

        if (pureName == null) {
            Utils.info("Invalid player name.");
            return 1;
        }

        GlowPlayer.addPlayer(pureName, color);
        String colorStr = String.format("#%06X", color.hex);
        Utils.info(pureName + " glow color set to " + colorStr + ".");
        applyGlowToOnlinePlayer(pureName);
        return 1;
    }

    private static int removeGlow(CommandContext<FabricClientCommandSource> ctx) {
        String rawName = StringArgumentType.getString(ctx, "player");
        String pureName = GlowPlayer.convertToPureName(rawName);

        if (pureName == null) {
            Utils.info("Invalid player name.");
            return 1;
        }

        boolean removed = GlowPlayer.removePlayer(pureName);
        Utils.info(
                removed
                        ? pureName + " will no longer glow."
                        : pureName + " was not glowing."
        );
        // Ensure any scoreboard/team state is cleaned up
        // GlowPlayer.removePlayer already restores teams on remove
        return 1;
    }

    /* ---------------- Listing ---------------- */

    private static void listGlows() {
        java.util.Set<String> names = GlowPlayer.getForcedNames();
        if (names.isEmpty()) {
            Utils.info("No forced glows.");
            return;
        }

        StringBuilder sb = new StringBuilder("Forced glows:\n");
        for (String name : names) {
            RenderColor color = GlowPlayer.getColor(name);
            String colorStr = color == null ? "none" : String.format("#%06X", color.hex);
            sb.append(name).append(" (").append(colorStr).append(")\n");
        }
        Utils.info(sb.toString());
    }

    /* ---------------- Suggestions ---------------- */

    private static CompletableFuture<Suggestions> suggestOnlinePlayers(
            CommandContext<FabricClientCommandSource> ctx,
            SuggestionsBuilder builder
    ) {
        if (mc.world == null) return builder.buildFuture();

        String remaining = builder.getRemaining().toLowerCase();

        for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
            if (Utils.isRealPlayer(player)) {
                String name = player.getName().getString();
                if (name.toLowerCase().startsWith(remaining)) {
                    builder.suggest(name);
                }
            }
        }

        return builder.buildFuture();
    }

    /* ---------------- Utilities ---------------- */

    private static void applyGlowToOnlinePlayer(String pureName) {
        if (mc.world == null) return;

        // Find and apply glow to all matching players
        for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
            if (!Utils.isRealPlayer(player)) continue;

            String playerPureName = GlowPlayer.convertToPureName(player.getName().getString());
            if (playerPureName == null || !playerPureName.equals(pureName)) continue;

            RenderColor glowColor = GlowPlayer.getColor(pureName);
            if (glowColor != null) {
                GlowPlayer.setGlowImmediately(player, glowColor);
            }
        }
    }


}
