package dev.protospoof.minebyte;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.server.MinecraftServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MineByteMod implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("minebyte");
    
    private static final String TOKEN = System.getenv("MINEBYTE_TOKEN");
    private static MinecraftServer server = null;
    private static MineByteBot botClient = null;

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        ServerMessageEvents.CHAT_MESSAGE.register((message, player, params) -> {
            botClient.sendChatMessage(player.getUuid(), player.getDisplayName().getString(), message.signedBody().content());
        });
        ServerMessageEvents.GAME_MESSAGE.register((server, message, overlay) -> {
            botClient.sendGameEvent(message.getString());
        });
        LOGGER.info("Initialized :D");
    }
    
    public static void serverStarting(MinecraftServer s) {
        server = s;
        botClient = new MineByteBot(TOKEN, server);
    }

    public static void serverStopped(MinecraftServer s) {
        server = null;
        botClient.sendStoppedServerMessage();
    }
}

