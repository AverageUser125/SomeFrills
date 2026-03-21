package com.somefrills.features.farming;

import com.somefrills.config.Feature;
import com.somefrills.events.ChatMsgEvent;
import com.somefrills.events.HudRenderEvent;
import com.somefrills.events.ServerJoinEvent;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

import static com.somefrills.Main.mc;

public class AutoPestSetHome {
    public static final Feature instance = new Feature("autoPestSetHome");

    // Ignore pest-control messages that arrive immediately after joining the server (client
    // often prints them when you join a garden). We only act on messages that appear after
    // this grace period.
    private static long lastServerJoinTime = 0L;
    private static final long IGNORE_WINDOW_MS = 10000L;

    @EventHandler
    private static void onServerJoin(ServerJoinEvent event) {
        if (!instance.isActive()) return;
        lastServerJoinTime = System.currentTimeMillis();
    }

    @EventHandler
    private static void onChatMessage(ChatMsgEvent event) {
        if (!instance.isActive()) return;
        if (event.messagePlain == null) return;

        if (!event.messagePlain.contains("Pest have spawned in Plot - ")) return;

        long now = System.currentTimeMillis();
        if (now - lastServerJoinTime < IGNORE_WINDOW_MS) {
            return;
        }
        if (!Utils.isInGarden()) return;

        if (mc.player != null && mc.player.networkHandler != null) {
            try {
                mc.player.networkHandler.sendChatCommand("sethome");
                Utils.info("AutoPestSetHome: executed /sethome");
            } catch (Throwable t) {
                // swallow — non-fatal
            }
        }
    }

    @EventHandler
    private  static void onHudTick(HudRenderEvent event) {
        if(!instance.isActive()) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;
        String title = "";
        if (client.currentScreen != null) {
            Text txt = client.currentScreen.getTitle();
            if (txt != null) title = txt.getString();
        }
        if (!title.contains("Trap")) return;

        lastServerJoinTime = System.currentTimeMillis();
    }
}
