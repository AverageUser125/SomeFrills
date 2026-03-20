package com.somefrills;

import com.somefrills.config.Config;
import com.somefrills.events.ChatMsgEvent;
import com.somefrills.events.ClientDisconnectEvent;
import com.somefrills.events.OverlayMsgEvent;
import com.somefrills.events.PartyChatMsgEvent;
import com.somefrills.features.farming.*;
import com.somefrills.features.solvers.*;
import com.somefrills.features.tweaks.*;
import com.somefrills.features.mining.*;
import com.somefrills.hud.ClickGui;
import com.somefrills.misc.Utils;
import com.mojang.brigadier.CommandDispatcher;
import com.somefrills.commands.SomeFrillsCommand;
import com.somefrills.features.solvers.GlowPlayer;
import io.wispforest.owo.config.ui.ConfigScreenProviders;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

import static net.fabricmc.loader.impl.FabricLoaderImpl.MOD_ID;

public class Main implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static MinecraftClient mc;
    public static IEventBus eventBus = new EventBus();

    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess access) {
        SomeFrillsCommand.init(dispatcher);
    }

    @Override
    public void onInitializeClient() {
        long start = Util.getMeasuringTimeMs();

        mc = MinecraftClient.getInstance();

        Config.load();
        ConfigScreenProviders.register("com.somefrills", screen -> new ClickGui());
        ClientCommandRegistrationCallback.EVENT.register(Main::registerCommands);

        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            String msg = Utils.toPlain(message);

            if (overlay) {
                return !eventBus.post(new OverlayMsgEvent(message, msg)).isCancelled();
            }

            boolean cancelled = eventBus.post(new ChatMsgEvent(message, msg)).isCancelled();
            if (msg.startsWith("Party > ") && msg.contains(": ")) {
                int nameStart = msg.contains("]") && msg.indexOf("]") < msg.indexOf(":") ? msg.indexOf("]") : msg.indexOf(">");
                String[] clean = msg.replace(msg.substring(0, nameStart + 1), "").split(":" , 2);
                String author = clean[0].trim(), content = clean[1].trim();
                cancelled = eventBus.post(new PartyChatMsgEvent(content, author)).isCancelled() || cancelled;
            }
            return !cancelled;
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            eventBus.post(new ClientDisconnectEvent());
        });

        eventBus.registerLambdaFactory("com.somefrills",
                (lookupInMethod, klass) -> (MethodHandles.Lookup)
                        lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        eventBus.subscribe(SpaceFarmer.class);
        eventBus.subscribe(Rewarp.class);
        eventBus.subscribe(MiddleClickOverride.class);
        eventBus.subscribe(BreakResetFix.class);
        eventBus.subscribe(DoubleUseFix.class);
        eventBus.subscribe(GemstoneDesyncFix.class);
        eventBus.subscribe(GhostVision.class);
        eventBus.subscribe(ChocolateFactory.class);
        eventBus.subscribe(ExperimentSolver.class);
        eventBus.subscribe(GlowPlayer.class);


        LOGGER.info("It's time to get real, NoFrills mod initialized in {}ms.", Util.getMeasuringTimeMs() - start);
    }
}
