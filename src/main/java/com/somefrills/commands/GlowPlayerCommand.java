package com.somefrills.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.somefrills.features.core.Features;
import com.somefrills.features.misc.GlowPlayer;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Utils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jspecify.annotations.NonNull;

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
                .then(literal("listglowing")
                        .executes(GlowPlayerCommand::listGlowingPlayers)
                        .then(argument("name", StringArgumentType.word())
                                .executes(ctx ->
                                        listGlowingPlayers(
                                                ctx,
                                                StringArgumentType.getString(ctx, "name")
                                        )
                                )
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
                .then(literal("clear")
                .executes(ctx -> {
                    if (!isGlowPlayerEnabled()) {
                        Utils.info("GlowPlayer feature is disabled.");
                        return 1;
                    }
                    get().clear();
                    Utils.info("Cleared all forced glows.");
                    return 1;
                }))
                .then(literal("remove")
                        .then(argument("player", StringArgumentType.word())
                                .suggests(GlowPlayerCommand::suggestGlowingPlayers)
                                .executes(ctx -> {
                                    if (!isGlowPlayerEnabled()) {
                                        Utils.info("GlowPlayer feature is disabled.");
                                        return 1;
                                    }
                                    return removeGlow(ctx);
                                })
                        )
                );
    }


    private static boolean isGlowPlayerEnabled() {
        if (!Features.isActive(GlowPlayer.class)) {
            Utils.info("GlowPlayer feature is disabled.");
            return false;
        }
        return true;
    }
    /* ---------------- Command handlers ---------------- */

    private static int addGlow(CommandContext<FabricClientCommandSource> ctx, Formatting color) {
        String name = StringArgumentType.getString(ctx, "player");

        if (name == null) {
            Utils.info("Invalid player name.");
            return 1;
        }

        RenderColor renderColor = RenderColor.fromFormatting(color);
        boolean added = get().addPlayer(name, renderColor);
        Utils.info(
                added
                        ? name + " will now glow (" + color.getName() + ")."
                        : name + " is already glowing."
        );
        applyGlowToOnlinePlayer(name);
        return 1;
    }

    private static int addGlowWithRenderColor(CommandContext<FabricClientCommandSource> ctx, RenderColor color) {
        String name = StringArgumentType.getString(ctx, "player");

        if (name == null) {
            Utils.info("Invalid player name.");
            return 1;
        }

        boolean added = get().addPlayer(name, color);
        String colorStr = String.format("#%06X", color.hex);
        Utils.info(
                added
                        ? name + " will now glow (" + colorStr + ")."
                        : name + " is already glowing."
        );
        applyGlowToOnlinePlayer(name);
        return 1;
    }

    private static int setColor(CommandContext<FabricClientCommandSource> ctx, Formatting color) {
        String name = StringArgumentType.getString(ctx, "player");

        if (name == null) {
            Utils.info("Invalid player name.");
            return 1;
        }

        // Set color even if the player wasn't previously added
        RenderColor renderColor = RenderColor.fromFormatting(color);
        get().addPlayer(name, renderColor);
        Utils.info(name + " glow color set to " + color.getName() + ".");
        applyGlowToOnlinePlayer(name);
        return 1;
    }

    private static int setColorWithRenderColor(CommandContext<FabricClientCommandSource> ctx, RenderColor color) {
        String name = StringArgumentType.getString(ctx, "player");

        if (name == null) {
            Utils.info("Invalid player name.");
            return 1;
        }

        get().addPlayer(name, color);
        String colorStr = String.format("#%06X", color.hex);
        Utils.info(name + " glow color set to " + colorStr + ".");
        applyGlowToOnlinePlayer(name);
        return 1;
    }

    private static int removeGlow(CommandContext<FabricClientCommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "player");
        if (name == null) {
            Utils.info("Invalid player name.");
            return 1;
        }

        boolean removed = get().removePlayer(name);
        Utils.info(
                removed
                        ? name + " will no longer glow."
                        : name + " was not glowing."
        );
        // Ensure any scoreboard/team state is cleaned up
        // GlowPlayer.removePlayer already restores teams on remove
        return 1;
    }

    /* ---------------- Listing ---------------- */

    private static void listGlows() {
        java.util.Set<String> names = get().getForcedNames();
        if (names.isEmpty()) {
            Utils.info("No forced glows.");
            return;
        }

        StringBuilder sb = new StringBuilder("Forced glows:\n");
        for (String name : names) {
            RenderColor color = get().getColor(name);
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

    private static CompletableFuture<Suggestions> suggestGlowingPlayers(
            CommandContext<FabricClientCommandSource> ctx,
            SuggestionsBuilder builder) {
        if (mc.world == null) return builder.buildFuture();

        String remaining = builder.getRemaining().toLowerCase();

        for (String name : get().getForcedNames()) {
            if (name.toLowerCase().startsWith(remaining)) {
                builder.suggest(name);
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

            String playerPureName = Utils.getPlayerName(player);
            if (playerPureName == null || !playerPureName.equals(pureName)) continue;

            RenderColor glowColor = get().getColor(pureName);
            if (glowColor != null) {
                get().setGlowImmediately(player, glowColor);
            }
        }
    }

    public static int listGlowingPlayers(CommandContext<FabricClientCommandSource> ctx) {
        return listGlowingPlayers(ctx, "");
    }

    public static int listGlowingPlayers(CommandContext<FabricClientCommandSource> ctx, String name) {
        if (mc.world == null) return 1;

        StringBuilder sb = new StringBuilder("Currently glowing players:\n");
        var forcedNames = get().getForcedNames();
        for (var player : mc.world.getPlayers()) {
            if (!Utils.isRealPlayer(player)) continue;

            String playerPureName = Utils.getPlayerName(player);
            if (!name.isEmpty()) {
                if (playerPureName == null || !playerPureName.toLowerCase().contains(name.toLowerCase())) continue;
            }
            if (!forcedNames.contains(playerPureName)) continue;

            sb.append("  - ")
                    .append(playerPureName)
                    .append("(")
                    .append(Utils.colorToString(get().getColor(playerPureName)))
                    .append(")")
                    .append(", Pos: [")
                    .append(String.format("%.1f", player.getX()))
                    .append(", ")
                    .append(String.format("%.1f", player.getY()))
                    .append(", ")
                    .append(String.format("%.1f", player.getZ()))
                    .append("]\n");
        }
        ctx.getSource().sendFeedback(Text.literal(sb.toString()));
        return 1;
    }

    public static @NonNull GlowPlayer get() {
        return Features.get(GlowPlayer.class);
    }
}
