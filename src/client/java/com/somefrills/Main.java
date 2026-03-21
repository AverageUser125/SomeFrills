package com.somefrills;

import com.mojang.brigadier.CommandDispatcher;
import com.somefrills.commands.SomeFrillsCommand;
import com.somefrills.config.Config;
import com.somefrills.config.FeatureRegistry;
import com.somefrills.events.*;
import com.somefrills.hud.clickgui.ClickGui;
import com.somefrills.misc.EntityCache;
import com.somefrills.misc.SkyblockData;
import com.somefrills.misc.Utils;
import io.wispforest.owo.config.ui.ConfigScreenProviders;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
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
                String[] clean = msg.replace(msg.substring(0, nameStart + 1), "").split(":", 2);
                String author = clean[0].trim(), content = clean[1].trim();
                cancelled = eventBus.post(new PartyChatMsgEvent(content, author)).isCancelled() || cancelled;
            }
            return !cancelled;
        });


        ClientTickEvents.END_CLIENT_TICK.register((client) -> eventBus.post(new EndTickEvent()));

        // Post ClientDisconnectEvent on Fabric disconnect and save config
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            eventBus.post(new ClientDisconnectEvent());
            // ensure config persists on disconnect
            Config.save();
        });

        // Save config on JVM shutdown (game close)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Config.save();
            } catch (Throwable t) {
                LOGGER.error("Error saving config on shutdown", t);
            }
        }));

        eventBus.registerLambdaFactory("com.somefrills",
                (lookupInMethod, klass) -> (MethodHandles.Lookup)
                        lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        eventBus.subscribe(SkyblockData.class);
        eventBus.subscribe(EntityCache.class);
        // initialize reflection-based registry which also subscribes discovered features
        FeatureRegistry.init();

        LOGGER.info("It's time to get real, NoFrills mod initialized in {}ms.", Util.getMeasuringTimeMs() - start);
    }
}
