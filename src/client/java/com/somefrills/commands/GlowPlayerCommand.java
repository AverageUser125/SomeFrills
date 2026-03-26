// TODO(Ravel): Failed to fully resolve file: null cannot be cast to non-null type com.intellij.psi.PsiJavaCodeReferenceElement
package com.somefrills.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.somefrills.features.misc.GlowPlayer;
import com.somefrills.misc.Utils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.AbstractClientPlayer;

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
                                    return addGlow(ctx, ChatFormatting.WHITE);
                                })
                                .then(argument("color", StringArgumentType.word())
                                        .suggests(GlowPlayerCommand::suggestColors)
                                        .executes(ctx -> {
                                            if (!GlowPlayer.instance.isActive()) {
                                                Utils.info("GlowPlayer feature is disabled.");
                                                return 1;
                                            }
                                            ChatFormatting color = Utils.parseColor(
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
                                            ChatFormatting color = Utils.parseColor(
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
                            GlowPlayer.clear();
                            Utils.info("Cleared all forced glows.");
                            return 1;
                        }))
                );
    }

    /* ---------------- Command handlers ---------------- */

    private static int addGlow(CommandContext<FabricClientCommandSource> ctx, ChatFormatting color) {
        String rawName = StringArgumentType.getString(ctx, "player");
        String pureName = GlowPlayer.convertToPureName(rawName);

        if (pureName == null) {
            Utils.info("Invalid player name.");
            return 1;
        }

        boolean added = GlowPlayer.addPlayer(pureName, color);
        Utils.info(
                added
                        ? pureName + " will now glow (" + color.getName() + ")."
                        : pureName + " is already glowing."
        );
        return 1;
    }

    private static int setColor(CommandContext<FabricClientCommandSource> ctx, ChatFormatting color) {
        String rawName = StringArgumentType.getString(ctx, "player");
        String pureName = GlowPlayer.convertToPureName(rawName);

        if (pureName == null) {
            Utils.info("Invalid player name.");
            return 1;
        }

        // Set color even if the player wasn't previously added
        GlowPlayer.addPlayer(pureName, color);
        Utils.info(pureName + " glow color set to " + color.getName() + ".");
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
            ChatFormatting color = GlowPlayer.getColor(name);
            sb.append(name).append(" (").append(color == null ? "none" : color.getName()).append(")\n");
        }
        Utils.info(sb.toString());
    }

    /* ---------------- Suggestions ---------------- */

    private static CompletableFuture<Suggestions> suggestOnlinePlayers(
            CommandContext<FabricClientCommandSource> ctx,
            SuggestionsBuilder builder
    ) {
        if (mc.level == null) return builder.buildFuture();

        String remaining = builder.getRemaining().toLowerCase();

        for (AbstractClientPlayer player : mc.level.players()) {
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
        for (ChatFormatting f : ChatFormatting.values()) {
            if (f.isColor() && f.getName().startsWith(remaining)) {
                builder.suggest(f.getName());
            }
        }
        return builder.buildFuture();
    }

    /* ---------------- Utilities ---------------- */

}
