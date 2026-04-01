package com.somefrills;

import com.mojang.brigadier.CommandDispatcher;
import com.somefrills.commands.SomeFrillsCommand;
import com.somefrills.config.Features;
import com.somefrills.config.FrillsConfig;
import com.somefrills.events.ChatMsgEvent;
import com.somefrills.events.ClientDisconnectEvent;
import com.somefrills.events.OverlayMsgEvent;
import com.somefrills.events.PartyChatMsgEvent;
import com.somefrills.features.misc.Aliases;
import com.somefrills.misc.EntityCache;
import com.somefrills.misc.SkyblockData;
import com.somefrills.misc.Utils;
import io.github.notenoughupdates.moulconfig.gui.GuiContext;
import io.github.notenoughupdates.moulconfig.gui.GuiElementComponent;
import io.github.notenoughupdates.moulconfig.managed.ManagedConfig;
import io.github.notenoughupdates.moulconfig.platform.MoulConfigScreenComponent;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;

public class Main implements ClientModInitializer {
    // Mod id - keep a local constant instead of relying on loader internals
    public static final String MOD_ID = "somefrills";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static MinecraftClient mc;
    public static IEventBus eventBus = new EventBus();
    public static ManagedConfig<FrillsConfig> config;

    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess access) {
        SomeFrillsCommand.init(dispatcher);
    }

    @Override
    public void onInitializeClient() {
        long start = Util.getMeasuringTimeMs();
        mc = MinecraftClient.getInstance();

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

        // Post ClientDisconnectEvent on Fabric disconnect and save config
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> eventBus.post(new ClientDisconnectEvent()));

        ClientSendMessageEvents.MODIFY_COMMAND.register(Aliases::convertCommand);
        config = ManagedConfig.create(new File("config/somefrills/config.json"), FrillsConfig.class);
        FrillsConfig.instance = config.getInstance();
        
        eventBus.registerLambdaFactory("com.somefrills",
                (lookupInMethod, klass) -> (MethodHandles.Lookup)
                        lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        eventBus.subscribe(SkyblockData.class);
        eventBus.subscribe(EntityCache.class);
        Features.init();
        config.rebuildConfigProcessor();

        LOGGER.info("It's time to get real, SomeFrills mod initialized in {}ms.", Util.getMeasuringTimeMs() - start);
    }
}
