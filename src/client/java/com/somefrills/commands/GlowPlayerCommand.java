package com.somefrills.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.somefrills.features.solvers.GlowPlayer;
import com.somefrills.misc.GlowManager;
import com.somefrills.misc.GlowTeamManager;
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
                    if (!GlowPlayer.instance.isActive()) {
                        Utils.info("GlowPlayer feature is disabled.");
                        return 1;
                    }
                    Utils.info("Usage: /glowplayer <list|add|color|remove>");
                    return 1;
                })
                .then(literal("list").executes(ctx -> {
                    if (!GlowPlayer.instance.isActive()) {
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
                                    if (!GlowPlayer.instance.isActive()) {
                                        Utils.info("GlowPlayer feature is disabled.");
                                        return 1;
                                    }
                                    return addGlow(ctx, Formatting.WHITE);
                                })
                                .then(argument("color", StringArgumentType.word())
                                        .suggests(GlowPlayerCommand::suggestColors)
                                        .executes(ctx -> {
                                            if (!GlowPlayer.instance.isActive()) {
                                                Utils.info("GlowPlayer feature is disabled.");
                                                return 1;
                                            }
                                            Formatting color = Utils.parseColor(
                                                    StringArgumentType.getString(ctx, "color")
                                            );
                                            if (color == null) {
                                                Utils.info("Invalid color.");
                                                return 1;
                                            }
                                            return addGlow(ctx, color);
                                        })
                                )
                        )
                )
                .then(literal("color")
                        .then(argument("player", StringArgumentType.word())
                                .suggests(GlowPlayerCommand::suggestOnlinePlayers)
                                .then(argument("color", StringArgumentType.word())
                                        .suggests(GlowPlayerCommand::suggestColors)
                                        .executes(ctx -> {
                                            if (!GlowPlayer.instance.isActive()) {
                                                Utils.info("GlowPlayer feature is disabled.");
                                                return 1;
                                            }
                                            Formatting color = Utils.parseColor(
                                                    StringArgumentType.getString(ctx, "color")
                                            );
                                            if (color == null) {
                                                Utils.info("Invalid color.");
                                                return 1;
                                            }
                                            return setColor(ctx, color);
                                        })
                                )
                        )
                )
                .then(literal("remove")
                        .then(argument("player", StringArgumentType.word())
                                .suggests(GlowPlayerCommand::suggestOnlinePlayers)
                                .executes(ctx -> {
                                    if (!GlowPlayer.instance.isActive()) {
                                        Utils.info("GlowPlayer feature is disabled.");
                                        return 1;
                                    }
                                    return removeGlow(ctx);
                                })
                        )
                        .then(literal("all").executes(ctx -> {
                            if (!GlowPlayer.instance.isActive()) {
                                Utils.info("GlowPlayer feature is disabled.");
                                return 1;
                            }
                            GlowManager.clear();
                            Utils.info("Cleared all forced glows.");
                            return 1;
                        }))
                );
    }

    /* ---------------- Command handlers ---------------- */

    private static int addGlow(CommandContext<FabricClientCommandSource> ctx, Formatting color) {
        String name = StringArgumentType.getString(ctx, "player");
        AbstractClientPlayerEntity player = findOnlinePlayer(name);

        if (player == null) {
            Utils.info("Player must be online to add glow!");
            return 1;
        }

        boolean added = GlowManager.add(player.getUuid(), color);
        Utils.info(
                added
                        ? player.getName().getString() + " will now glow (" + color.getName() + ")."
                        : player.getName().getString() + " is already glowing."
        );
        return 1;
    }

    private static int setColor(CommandContext<FabricClientCommandSource> ctx, Formatting color) {
        String name = StringArgumentType.getString(ctx, "player");
        AbstractClientPlayerEntity player = findOnlinePlayer(name);

        if (player == null) {
            Utils.info("Player must be online to change glow color!");
            return 1;
        }

        if (!GlowManager.has(player.getUuid())) {
            Utils.info(player.getName().getString() + " is not glowing.");
            return 1;
        }

        GlowManager.add(player.getUuid(), color);
        Utils.info(player.getName().getString() + " glow color set to " + color.getName() + ".");
        return 1;
    }

    private static int removeGlow(CommandContext<FabricClientCommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "player");
        AbstractClientPlayerEntity player = findOnlinePlayer(name);

        if (player == null) {
            Utils.info("Player must be online to remove glow!");
            return 1;
        }

        boolean removed = GlowManager.remove(player.getUuid());
        Utils.info(
                removed
                        ? player.getName().getString() + " will no longer glow."
                        : player.getName().getString() + " was not glowing."
        );
        GlowTeamManager.remove(player.getName().getString());
        // Never removed from GlowTeamManager since I don't care
        return 1;
    }

    /* ---------------- Listing ---------------- */

    private static void listGlows() {
        if (mc.world == null) return;

        StringBuilder sb = new StringBuilder("Forced glows:\n");
        boolean any = false;

        for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
            Formatting color = GlowManager.getColor(player.getUuid());
            if (color != null) {
                sb.append(player.getName().getString())
                        .append(" (")
                        .append(color.getName())
                        .append(")\n");
                any = true;
            }
        }

        if (!any) sb = new StringBuilder("No forced glows.");
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

    private static CompletableFuture<Suggestions> suggestColors(
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

    /* ---------------- Utilities ---------------- */

    private static AbstractClientPlayerEntity findOnlinePlayer(String name) {
        if (mc.world == null) return null;

        for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
            if (Utils.isRealPlayer(player)
                    && player.getName().getString().equalsIgnoreCase(name)) {
                return player;
            }
        }
        return null;
    }

}
