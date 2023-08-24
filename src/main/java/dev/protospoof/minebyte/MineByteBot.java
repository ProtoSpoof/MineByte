package dev.protospoof.minebyte;

import java.util.EnumSet;
import java.util.UUID;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.minecraft.server.MinecraftServer;

public class MineByteBot {
    
    private final long channelID = 1133909854528999454l;

    private MinecraftServer server = null;
    private JDA botClient;
    private EmbedBuilder embedBuilder = new EmbedBuilder();
    
    // Colors
    private int colorSuccess = 0x42f593;
    private int colorFailure = 0xf54251;
    private int colorInProgress = 0x42e0f5;
    private int colorChatMessage = 0xf542ef;
    private int colorSystemMessage = 0xf5ce42;

    protected TextChannel chatChannel = null;

    public MineByteBot(String token, MinecraftServer server) {
        this.server = server;
        this.botClient = JDABuilder.createLight(token)
            .enableIntents(EnumSet.allOf(GatewayIntent.class))
            .setActivity(Activity.playing("Working on Myself"))
            .build();
        this.botClient.addEventListener(new BotEventListener());
        this.updateSlashCommands();
    }

    public void updateSlashCommands() {
        botClient.updateCommands().addCommands(
            Commands.slash("whitelist", "Add yourself to the Minecraft server whitelist")
                .setGuildOnly(true)
                .addOption(OptionType.STRING, "username", "Your Minecraft Username", true, false),
            Commands.slash("unwhitelist", "Remove yourself from the Minecraft server whitelist")
                .setGuildOnly(true)
                .addOption(OptionType.STRING, "username", "Your Minecraft Username", true, false),
            Commands.slash("restart", "Restart the Minecraft Server")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
            Commands.slash("sendcommand", "Send a Command to the Minecraft Server")
                .setGuildOnly(true)
                .addOption(OptionType.STRING, "command", "The Command to Send", true, false)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
        ).queue();
    }

    public void sendGameEvent(String message) {
        this.embedBuilder.clear();
        this.embedBuilder.setDescription(message)
            .setColor(colorSystemMessage);
        this.chatChannel.sendMessageEmbeds(this.embedBuilder.build()).queue();
    }

    public void sendChatMessage(UUID uid, String username, String message) {
        this.embedBuilder.clear();
        this.embedBuilder.setAuthor(username, null, String.format("https://mc-heads.net/avatar/%s.png", uid.toString()))
            .setDescription(message)
            .setColor(colorChatMessage);
        this.chatChannel.sendMessageEmbeds(this.embedBuilder.build()).queue();
    }

    protected void sendStartedServerMessage() {
        this.embedBuilder.clear();
        this.embedBuilder.setTitle("Server Started")
                .setDescription("The Server has been sucessfully started")
                .setColor(this.colorSuccess);
        this.chatChannel.sendMessageEmbeds(this.embedBuilder.build()).queue();
    }

    protected void sendStoppedServerMessage() {
        this.embedBuilder.clear();
        this.embedBuilder.setTitle("Server Stopped")
                .setDescription("The Server has been sucessfully stopped")
                .setColor(this.colorSuccess);
        this.chatChannel.sendMessageEmbeds(this.embedBuilder.build()).queue();
    }

    private class BotEventListener extends ListenerAdapter {
        @Override
        public void onReady(ReadyEvent event) {
            chatChannel = botClient.getTextChannelById(channelID);
            sendStartedServerMessage();
        }

        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            if (event.getAuthor().isBot() || !event.getChannel().getId().equals(chatChannel.getId())) return;

            String discordMessageAuthor = event.getMember().getEffectiveName();
            String discordMessage = event.getMessage().getContentStripped();
            String command = String.format("tellraw @a [\"\",{\"text\":\"[Discord]\",\"bold\":true,\"color\":\"dark_purple\"},{\"text\": \" <%s>\", \"color\":\"white\"},{\"text\":\" %s\",\"color\":\"white\"}]", discordMessageAuthor, discordMessage);

            server.getCommandManager().executeWithPrefix(server.getCommandSource(), command);
        }

        @Override
        public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
            switch (event.getName()) {
                case "whitelist": 
                     server.getCommandManager().executeWithPrefix(server.getCommandSource(), String.format("/whitelist add %s", event.getOption("username", OptionMapping::getAsString)));
                    event.reply("You've been added to the whitelist!").setEphemeral(true).queue();
                break;
                
                case "unwhitelist": 
                    server.getCommandManager().executeWithPrefix(server.getCommandSource(), String.format("/whitelist add %s", event.getOption("username")));
                    event.reply("You've been removed from the whitelist.").setEphemeral(true).queue();
                break;

                case "restart":
                    event.reply("Restarting the Server!").setEphemeral(true).queue();
                    server.stop(true);
                break;

                case "sendcommand":
                    String commandToSend = event.getOption("command", OptionMapping::getAsString);
                    if (commandToSend.charAt(0) != '/') commandToSend = "/".concat(commandToSend);
                    server.getCommandManager().executeWithPrefix(server.getCommandSource(), commandToSend);
                    event.reply("Command Sent!").setEphemeral(true).queue();
                break;
            }
        }
    }
}