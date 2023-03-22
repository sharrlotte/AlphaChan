package AlphaChan.main.handler;

import mindustry.game.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.*;
import net.dv8tion.jda.api.hooks.*;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import javax.annotation.Nonnull;

import org.bson.Document;

import AlphaChan.main.data.user.GuildData;
import AlphaChan.main.data.user.UserData;
import AlphaChan.main.data.user.GuildData.CHANNEL_TYPE;
import AlphaChan.main.handler.ContentHandler.Map;
import AlphaChan.main.handler.DatabaseHandler.LOG_TYPE;
import AlphaChan.main.util.Log;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static AlphaChan.AlphaChan.jda;

public final class MessageHandler extends ListenerAdapter {

    private static MessageHandler instance = new MessageHandler();

    public final Integer messageAliveTime = 30;

    public HashMap<String, TextChannel> serverChatChannel = new HashMap<String, TextChannel>();

    private MessageHandler() {

        jda.addEventListener(this);
        Log.system("Message handler up");
    }

    public static MessageHandler getInstance() {
        if (instance == null)
            instance = new MessageHandler();
        return instance;
    }

    private static String getMessageSender(Guild guild, Category category, Channel channel, Member member) {
        String guildName = guild == null ? "Unknown" : guild.getName();
        String categoryName = category == null ? "Unknown" : category.getName();
        String channelName = channel == null ? "Unknown" : channel.getName();
        String memberName = member == null ? "Unknown" : member.getEffectiveName();

        return "[" + guildName + "] " + "<" + categoryName + ":" + channelName + "> " + memberName;

    }

    public static String getMessageSender(Message message) {
        return getMessageSender(message.getGuild(), message.getCategory(), message.getChannel(), message.getMember());
    }

    public static String getMessageSender(@Nonnull SlashCommandInteractionEvent event) {
        return getMessageSender(event.getGuild(), null, event.getChannel(), event.getMember());
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        Message message = event.getMessage();
        if (message.getAuthor().isBot())
            return;
        // Process the message
        handleMessage(message);
    }

    public static void handleMessage(Message message) {
        try {
            // Log all message that has been sent
            List<Attachment> attachments = message.getAttachments();
            Member member = message.getMember();

            // Schematic preview
            if ((ContentHandler.isSchematicText(message) && attachments.isEmpty()) || ContentHandler
                    .isSchematicFile(attachments)) {
                Log.system(getMessageSender(message) + ": sent a schematic ");
                sendSchematicPreview(message);
            }

            else if (ContentHandler.isMapFile(attachments)) {
                sendMapPreview(message, message.getChannel());
            }

            // Delete in channel that it should not be
            GuildData guildData = GuildHandler.getGuild(message.getGuild());

            guildData.resetTimer();
            if (guildData._containsChannel(CHANNEL_TYPE.SCHEMATIC.name(), message.getChannel().getId()) || //
                    guildData._containsChannel(CHANNEL_TYPE.MAP.name(), message.getChannel().getId())) {

                if (!message.getContentRaw().isEmpty()) {
                    message.delete().queue();
                    replyMessage(message, "Vui lòng không gửi tin nhắn vào kênh này!", 30);
                }
            }

            // Update exp on message sent
            UserHandler.onMessage(message);
            DatabaseHandler.log(LOG_TYPE.MESSAGE, new Document()//
                    .append("message", getMessageSender(message) + ": " + message.getContentDisplay())//
                    .append("messageId", message.getId())//
                    .append("userId", member == null ? null : member.getId())//
                    .append("guildId", message.getGuild().getId()));

            // Log member message/file/image url to terminals
            if (!message.getContentRaw().isEmpty())
                Log.print("LOG", getMessageSender(message) + ": " + message.getContentDisplay());

            else if (!message.getAttachments().isEmpty())
                message.getAttachments().forEach(attachment -> {
                    Log.print("LOG", getMessageSender(message) + ": " + attachment.getUrl());
                });

        } catch (Exception e) {
            Log.error(e);
        }

    }

    @Override
    public void onGuildMemberUpdateNickname(@Nonnull GuildMemberUpdateNicknameEvent event) {
        Member member = event.getMember();
        Member bot = event.getGuild().getMember(jda.getSelfUser());
        if (member == bot)
            return;
        Member target = event.getEntity();
        UserData userData = UserHandler.getUserNoCache(target);
        if (userData == null)
            throw new IllegalStateException("No user data found");

        if (bot == null)
            throw new IllegalStateException("Bot not in guild");
        userData._displayLevelName();

    }

    @Override
    public void onMessageDelete(@Nonnull MessageDeleteEvent event) {
        DatabaseHandler.log(LOG_TYPE.MESSAGE_DELETED, new Document("messageId", event.getMessageId()));
    }

    @Override
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {
        // Send invite link to member who left the guild
        User user = event.getUser();
        List<TextChannel> inviteChannels = event.getGuild().getTextChannels();
        if (!inviteChannels.isEmpty()) {
            Invite invite = inviteChannels.get(0).createInvite().complete();
            user.openPrivateChannel().queue(channel -> channel.sendMessage(invite.getUrl()).queue());
        }
        log(event.getGuild(), user.getName() + " rời máy chủ");
    }

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        UserHandler.addUser(event.getMember());
        log(event.getGuild(), event.getMember().getEffectiveName() + " tham gia máy chủ");
    }

    public static void log(Guild guild, @Nonnull String content) {
        GuildData guildData = GuildHandler.getGuild(guild);
        if (guildData == null)
            throw new IllegalStateException("No guild data found");

        List<TextChannel> botLogChannel = guildData._getChannels(CHANNEL_TYPE.BOT_LOG.name());
        if (botLogChannel == null) {
            Log.error("Bot log channel for guild <" + guild.getName() + "> does not exists");
        } else
            botLogChannel.forEach(c -> c.sendMessage("```" + content + "```").queue());
    }

    public static boolean isChannel(Guild guild, Channel channel,
            HashMap<String, HashMap<String, String>> guildChannelIds) {
        if (guildChannelIds.containsKey(guild.getId())) {
            if (guildChannelIds.get(guild.getId()).containsKey(channel.getId()))
                return true;
        }
        return false;
    }

    public static boolean hasChannel(@Nonnull String guildId, @Nonnull String channelId) {
        Guild guild = jda.getGuildById(guildId);
        if (guild == null)
            return false;
        List<GuildChannel> channel = guild.getChannels();
        for (GuildChannel c : channel) {
            if (c.getId().equals(channelId))
                return true;
        }
        return false;
    }

    public static void sendMapPreview(Attachment attachment, Member member, MessageChannel channel) {
        try {
            Map map = ContentHandler.readMap(NetworkHandler.download(attachment.getUrl()));
            File mapFile = ContentHandler.getMapFile(attachment);
            File mapImageFile = ContentHandler.getMapImageFile(map);
            EmbedBuilder builder = ContentHandler.getMapEmbedBuilder(map, mapFile, mapImageFile, member);

            channel.sendFiles(FileUpload.fromData(mapFile), FileUpload.fromData(mapImageFile))
                    .setEmbeds(builder.build()).queue();

        } catch (Exception e) {
            Log.error(e);
        }
    }

    public static void sendMapPreview(Message message, MessageChannel channel) {
        for (int i = 0; i < message.getAttachments().size(); i++) {
            Attachment attachment = message.getAttachments().get(i);
            if (ContentHandler.isMapFile(attachment)) {
                sendMapPreview(attachment, message.getMember(), channel);
                message.delete().queue();
            }
        }
    }

    public static void sendMapPreview(SlashCommandInteractionEvent event) {
        OptionMapping fileOption = event.getOption("mapfile");
        if (fileOption == null)
            return;
        Attachment attachment = fileOption.getAsAttachment();

        if (!ContentHandler.isMapFile(attachment)) {
            event.reply("File được chọn không phải là file bản đồ");
            return;
        }

        Member member = event.getMember();
        sendMapPreview(attachment, member, event.getChannel());
        event.reply("Gửi thành công.");
    }

    public static void sendSchematicPreview(SlashCommandInteractionEvent event) {
        OptionMapping fileOption = event.getOption("schematicfile");
        if (fileOption == null)
            return;
        Attachment attachment = fileOption.getAsAttachment();

        if (!ContentHandler.isSchematicFile(attachment)) {
            event.reply("File được chọn không phải là file bản thiết kế");
            return;
        }
        Member member = event.getMember();
        try {
            sendSchematicPreview(ContentHandler.parseSchematicURL(attachment.getUrl()), member, event.getChannel());
        } catch (Exception e) {
            Log.error(e);
        }
    }

    public static void sendSchematicPreview(Message message) {
        try {
            if (ContentHandler.isSchematicText(message)) {
                sendSchematicPreview(ContentHandler.parseSchematic(message.getContentRaw()), message);
            } else {
                for (int i = 0; i < message.getAttachments().size(); i++) {
                    Attachment attachment = message.getAttachments().get(i);

                    if (ContentHandler.isSchematicFile(attachment)) {
                        sendSchematicPreview(ContentHandler.parseSchematicURL(attachment.getUrl()), message);
                    }
                }
            }
        } catch (Exception e) {
            Log.error(e);
        }
        message.delete().queue();
    }

    public static void sendSchematicPreview(Schematic schem, Message message) {
        sendSchematicPreview(schem, message.getMember(), message.getChannel());
    }

    public static void sendSchematicPreview(Schematic schem, Member member, MessageChannel channel) {
        try {
            File schemFile = ContentHandler.getSchematicFile(schem);
            File previewFile = ContentHandler.getSchematicPreviewFile(schem);
            EmbedBuilder builder = ContentHandler.getSchematicEmbedBuilder(schem, previewFile, member);

            channel.sendFiles(FileUpload.fromData(schemFile), FileUpload.fromData(previewFile))
                    .setEmbeds(builder.build()).queue();
        } catch (Exception e) {
            sendMessage(channel, "Lỗi: " + e.getMessage(), 30);
        }
    }

    // Message send commands
    public static void replyMessage(SlashCommandInteractionEvent event, String content, int deleteAfter) {
        sendMessage(event.getChannel(), content, deleteAfter);
    }

    public static void sendMessage(MessageChannel channel, String content, int deleteAfter) {
        if (channel != null)
            channel.sendMessage("```" + content + "```")
                    .queue(m -> m.delete().queueAfter(deleteAfter, TimeUnit.SECONDS));
    }

    public static void replyMessage(Message message, String content, int deleteAfter) {
        message.reply("```" + content + "```").queue(m -> m.delete().queueAfter(deleteAfter, TimeUnit.SECONDS));
    }

    public static void replyMessage(Message message, String content) {
        message.reply("```" + content + "```").queue();
    }

    public static void sendEmbed(SlashCommandInteractionEvent event, EmbedBuilder builder, int deleteAfter) {
        event.getHook().sendMessageEmbeds(builder.build())
                .queue(_message -> _message.delete().queueAfter(deleteAfter, TimeUnit.SECONDS));
    }

    public static void sendEmbed(MessageContextInteractionEvent event, EmbedBuilder builder, int deleteAfter) {
        event.getHook().sendMessageEmbeds(builder.build())
                .queue(_message -> _message.delete().queueAfter(deleteAfter, TimeUnit.SECONDS));
    }

    public static void sendMessage(MessageContextInteractionEvent event, String content, int deleteAfter) {
        event.getHook().sendMessage("```" + content + "```")
                .queue(_message -> _message.delete().queueAfter(deleteAfter, TimeUnit.SECONDS));
    }
}
