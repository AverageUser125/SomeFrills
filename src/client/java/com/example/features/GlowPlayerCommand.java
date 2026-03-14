package com.example.features;

import com.example.utils.GlowManager;
import com.example.utils.GlowTeamManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import static com.example.Main.mc;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class GlowPlayerCommand {

    private static final Set<String> EXTRA_DISPLAY_NPC_BY_NAME = Set.of(
            "Guy ",
            "vswiblxdxg",
            "anrrtqytsl"
    );

    private static final Pattern DISPLAY_NPC_COMPRESSED_NAME_PATTERN =
            Pattern.compile("[a-z0-9]{10}");

    public GlowPlayerCommand() {
        ClientCommandRegistrationCallback.EVENT.register(
                (dispatcher, environment) -> registerCommands(dispatcher)
        );
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null) return;
            GlowTeamManager.init();
            for (AbstractClientPlayerEntity player : client.world.getPlayers()) {
                Formatting color = GlowManager.getColor(player.getUuid());
                if (color != null) {
                    GlowTeamManager.assign(player.getName().getString(), color);
                }
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            GlowManager.clear();
            GlowTeamManager.clear();
        });
    }

    /* ---------------- Commands ---------------- */

    private void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                literal("glowplayer")
                        .then(literal("list").executes(ctx -> {
                            listGlows();
                            return 1;
                        }))
                        .then(literal("add")
                                .then(argument("player", StringArgumentType.word())
                                        .suggests(this::suggestOnlinePlayers)
                                        .executes(ctx -> addGlow(ctx, Formatting.WHITE))
                                        .then(argument("color", StringArgumentType.word())
                                                .suggests(this::suggestColors)
                                                .executes(ctx -> {
                                                    Formatting color = parseColor(
                                                            StringArgumentType.getString(ctx, "color")
                                                    );
                                                    if (color == null) {
                                                        sendMessage("Invalid color.");
                                                        return 1;
                                                    }
                                                    return addGlow(ctx, color);
                                                })
                                        )
                                )
                        )
                        .then(literal("color")
                                .then(argument("player", StringArgumentType.word())
                                        .suggests(this::suggestOnlinePlayers)
                                        .then(argument("color", StringArgumentType.word())
                                                .suggests(this::suggestColors)
                                                .executes(ctx -> {
                                                    Formatting color = parseColor(
                                                            StringArgumentType.getString(ctx, "color")
                                                    );
                                                    if (color == null) {
                                                        sendMessage("Invalid color.");
                                                        return 1;
                                                    }
                                                    return setColor(ctx, color);
                                                })
                                        )
                                )
                        )
                        .then(literal("remove")
                                .then(argument("player", StringArgumentType.word())
                                        .suggests(this::suggestOnlinePlayers)
                                        .executes(this::removeGlow)
                                )
                                .then(literal("all").executes(ctx -> {
                                    GlowManager.clear();
                                    sendMessage("Cleared all forced glows.");
                                    return 1;
                                }))
                        )
        );
    }

    /* ---------------- Command handlers ---------------- */

    private int addGlow(CommandContext<FabricClientCommandSource> ctx, Formatting color) {
        String name = StringArgumentType.getString(ctx, "player");
        AbstractClientPlayerEntity player = findOnlinePlayer(name);

        if (player == null) {
            sendMessage("Player must be online to add glow!");
            return 1;
        }

        boolean added = GlowManager.add(player.getUuid(), color);
        sendMessage(
                added
                        ? player.getName().getString() + " will now glow (" + color.getName() + ")."
                        : player.getName().getString() + " is already glowing."
        );
        return 1;
    }

    private int setColor(CommandContext<FabricClientCommandSource> ctx, Formatting color) {
        String name = StringArgumentType.getString(ctx, "player");
        AbstractClientPlayerEntity player = findOnlinePlayer(name);

        if (player == null) {
            sendMessage("Player must be online to change glow color!");
            return 1;
        }

        if (!GlowManager.has(player.getUuid())) {
            sendMessage(player.getName().getString() + " is not glowing.");
            return 1;
        }

        GlowManager.add(player.getUuid(), color);
        sendMessage(player.getName().getString() + " glow color set to " + color.getName() + ".");
        return 1;
    }

    private int removeGlow(CommandContext<FabricClientCommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "player");
        AbstractClientPlayerEntity player = findOnlinePlayer(name);

        if (player == null) {
            sendMessage("Player must be online to remove glow!");
            return 1;
        }

        boolean removed = GlowManager.remove(player.getUuid());
        sendMessage(
                removed
                        ? player.getName().getString() + " will no longer glow."
                        : player.getName().getString() + " was not glowing."
        );
        GlowTeamManager.remove(player.getName().getString());
        // Never removed from GlowTeamManager since I don't care
        return 1;
    }

    /* ---------------- Listing ---------------- */

    private void listGlows() {
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
        sendMessage(sb.toString());
    }

    /* ---------------- Suggestions ---------------- */

    private CompletableFuture<Suggestions> suggestOnlinePlayers(
            CommandContext<FabricClientCommandSource> ctx,
            SuggestionsBuilder builder
    ) {
        if (mc.world == null) return builder.buildFuture();

        String remaining = builder.getRemaining().toLowerCase();

        for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
            if (isRealPlayer(player)) {
                String name = player.getName().getString();
                if (name.toLowerCase().startsWith(remaining)) {
                    builder.suggest(name);
                }
            }
        }

        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestColors(
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

    private Formatting parseColor(String input) {
        Formatting f = Formatting.byName(input.toLowerCase());
        return (f != null && f.isColor()) ? f : null;
    }

    private boolean isRealPlayer(AbstractClientPlayerEntity player) {
        if (player == null) return false;

        UUID uuid = player.getUuid();
        if (uuid == null || uuid.version() != 4) return false;

        String name = player.getName().getString();
        if (name.isEmpty()) return false;

        if (name.charAt(0) == '§' || name.charAt(0) == '!') return false;
        if (DISPLAY_NPC_COMPRESSED_NAME_PATTERN.matcher(name).matches()) return false;
        return !EXTRA_DISPLAY_NPC_BY_NAME.contains(name);
    }

    private AbstractClientPlayerEntity findOnlinePlayer(String name) {
        if (mc.world == null) return null;

        for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
            if (isRealPlayer(player)
                    && player.getName().getString().equalsIgnoreCase(name)) {
                return player;
            }
        }
        return null;
    }

    private void sendMessage(String message) {
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal(message), false);
        }
    }
}
