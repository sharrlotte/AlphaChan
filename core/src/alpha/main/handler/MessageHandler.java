package alpha.main.handler;

import mindustry.game.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.*;
import net.dv8tion.jda.api.hooks.*;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import org.bson.Document;

import alpha.main.data.user.GuildCache;

import alpha.main.data.user.GuildCache.ChannelType;
import alpha.main.handler.ContentHandler.Map;
import alpha.main.handler.DatabaseHandler.LogCollection;
import alpha.main.util.Log;
import alpha.main.util.StringUtils;

import static alpha.main.AlphaChan.jda;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class MessageHandler extends ListenerAdapter {

    private static MessageHandler instance = new MessageHandler();

    public final Integer messageAliveTime = 30;

    public HashMap<String, TextChannel> serverChatChannel = new HashMap<String, TextChannel>();

    private MessageHandler() {

        jda.addEventListener(this);
        Log.system("Message handler up");
    }

    public synchronized static MessageHandler getInstance() {
        if (instance == null)
            instance = new MessageHandler();
        return instance;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();

        if (message.getAuthor().isBot())
            return;

        // Log member message/file/image url to terminals
        if (!message.getContentRaw().isEmpty())
            Log.print("LOG", getMessageSender(message) + ": " + message.getContentDisplay());

        else if (!message.getAttachments().isEmpty())
            message.getAttachments()
                    .forEach(attachment -> Log.print("LOG", getMessageSender(message) + ": " + attachment.getUrl()));

        if (message.getContentRaw().startsWith("/") && UserHandler.isYui(message.getMember())) {
            CommandHandler.ConsoleCommandHandler.onCommand(message.getContentRaw());
            return;
        }

        // Process the message
        handleMessage(message);
    }

    public static void handleMessage(Message message) {
        try {
            // Log all message that has been sent
            List<Attachment> attachments = message.getAttachments();
            Member member = message.getMember();

            // Schematic preview
            if ((ContentHandler.isSchematicText(message) && attachments.isEmpty())
                    || ContentHandler.isSchematicFile(attachments)) {
                sendSchematicPreview(message);
                Log.system(getMessageSender(message) + ": sent a schematic ");
            }

            else if (ContentHandler.isMapFile(attachments)) {
                sendMapPreview(message, message.getChannel());
                Log.system(getMessageSender(message) + ": sent a map ");
            }

            // Delete in channel that it should not be
            GuildCache guildData = GuildHandler.getGuild(message.getGuild());
            guildData.resetTimer();

            boolean isSchematicChannel = guildData.hasChannel(ChannelType.SCHEMATIC, message.getChannel().getId());
            boolean isMapChannel = guildData.hasChannel(ChannelType.MAP, message.getChannel().getId());

            boolean isSchematicMessage = ContentHandler.isSchematicText(message)
                    || ContentHandler.isSchematicFile(attachments);
            boolean isMapMessage = ContentHandler.isMapFile(attachments);

            if ((isMapChannel && !isMapMessage) || (isSchematicChannel && !isSchematicMessage)) {
                message.delete().queue();
                replyMessage(message, "<message.no_message_here>[Don't send message here]", 30);

            } else {
                // Update level, money on message sent
                UserHandler.onMessage(message);
            }

            DatabaseHandler.log(LogCollection.MESSAGE, new Document()//
                    .append("message", getMessageSender(message) + ": " + message.getContentDisplay())//
                    .append("messageId", message.getId())//
                    .append("userId", member == null ? null : member.getId())//
                    .append("guildId", message.getGuild().getId()));

        } catch (Exception e) {
            Log.error(e);
        }

    }

    // @Override
    // public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event)
    // {
    // Member member = event.getMember();
    // Member bot = event.getGuild().getSelfMember();
    // if (member == bot)
    // return;

    // Member target = event.getEntity();
    // log(event.getGuild(), " <message.change_name_to> " +
    // target.getEffectiveName());

    // }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        DatabaseHandler.log(LogCollection.MESSAGE_DELETED, "messageId", event.getMessageId());
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        // Send invite link to member who left the guild
        User user = event.getUser();
        List<TextChannel> inviteChannels = event.getGuild().getTextChannels();
        if (!inviteChannels.isEmpty()) {
            // Create a never expire, no uses limit invite
            Invite invite = inviteChannels.get(0).createInvite().setMaxUses(0).setMaxAge(0).complete();
            user.openPrivateChannel().queue(channel -> channel.sendMessage(invite.getUrl()).queue());
        }
        log(event.getGuild(),
                LocaleManager.format(event.getGuild(), " <message.leave_guild>[User %s left guild]", user.getName()));

    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        UserHandler.addUser(event.getMember());
        log(event.getGuild(), LocaleManager.format(event.getGuild(), " <message.join_guild>[User %s joined guild]",
                event.getMember().getEffectiveName()));
    }

    public static void log(Guild guild, String content) {
        GuildCache guildData = GuildHandler.getGuild(guild);

        List<TextChannel> botLogChannel = guildData.getChannels(ChannelType.BOT_LOG);
        if (botLogChannel == null) {
            Log.error("Bot log channel for guild [" + guild.getName() + "] does not exists");

        } else
            botLogChannel.forEach(c -> c.sendMessage("```" + content + "```").queue());
    }

    private static String getMessageSender(Guild guild, Category category, Channel channel, Member member) {
        String guildName = guild == null ? "Unknown" : guild.getName();
        String categoryName = category == null ? "Unknown" : category.getName();
        String channelName = channel == null ? "Unknown" : channel.getName();
        String memberName = member == null ? "Unknown" : member.getEffectiveName();

        return "[" + guildName + "] " + "[" + categoryName + ":" + channelName + "] " + memberName;
    }

    public static String getMessageSender(Message message) {
        return getMessageSender(message.getGuild(), message.getCategory(), message.getChannel(), message.getMember());
    }

    public static String getMessageSender(SlashCommandInteractionEvent event) {
        return getMessageSender(event.getGuild(), null, event.getChannel(), event.getMember());
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
            MessageHandler.replyTranslate(event.getHook(), StringUtils.backtick(
                    "<error.not_a_map_file>[Error: This message doesn't contain any map files, map file should start with .msav]"),
                    10);
            return;
        }

        Member member = event.getMember();
        sendMapPreview(attachment, member, event.getChannel());
    }

    public static void sendSchematicPreview(SlashCommandInteractionEvent event) {
        OptionMapping fileOption = event.getOption("schematicfile");
        if (fileOption == null)
            return;
        Attachment attachment = fileOption.getAsAttachment();

        if (!ContentHandler.isSchematicFile(attachment)) {
            MessageHandler.replyTranslate(event.getHook(), StringUtils.backtick(
                    "<error.not_a_schematic_file>[Error: Message doesn't contain any schematic files, schematic file should start with .msch]"),
                    10);
            return;
        }
        Member member = event.getMember();
        try {
            sendSchematicPreview(ContentHandler.parseSchematicURL(attachment.getUrl()), member,
                    event.getGuildChannel());
        } catch (Exception e) {
            Log.error(e);
        }
    }

    public static void sendSchematicPreview(Message message) {
        try {
            if (ContentHandler.isSchematicText(message)) {
                sendSchematicPreview(ContentHandler.parseSchematic(message.getContentRaw()), message);
                return;
            }

            for (int i = 0; i < message.getAttachments().size(); i++) {
                Attachment attachment = message.getAttachments().get(i);
                if (ContentHandler.isSchematicFile(attachment)) {
                    sendSchematicPreview(ContentHandler.parseSchematicURL(attachment.getUrl()), message);
                }
            }

        } catch (Exception e) {
            Log.error(e);
        }
        message.delete().queue();
    }

    public static void sendSchematicPreview(Schematic schem, Message message) {
        sendSchematicPreview(schem, message.getMember(), message.getGuildChannel());
    }

    public static void sendSchematicPreview(Schematic schem, Member member, GuildMessageChannel channel) {
        try {
            File schemFile = ContentHandler.getSchematicFile(schem);
            File previewFile = ContentHandler.getSchematicPreviewFile(schem);

            EmbedBuilder builder = ContentHandler.getSchematicInfoEmbedBuilder(schem, member);
            List<Field> fields = builder.getFields();
            fields.add(2, new Field(EmbedBuilder.ZERO_WIDTH_SPACE, EmbedBuilder.ZERO_WIDTH_SPACE, false));

            builder.setAuthor(member.getEffectiveName(), member.getEffectiveAvatarUrl(), member.getEffectiveAvatarUrl())
                    .setTitle(schem.name()).setImage("attachment://" + previewFile.getName());

            channel.sendFiles(FileUpload.fromData(schemFile), FileUpload.fromData(previewFile))
                    .setEmbeds(getFormatEmbedBuilder(channel.getGuild(), builder).build()).queue();
        } catch (Exception e) {
            sendMessage(channel, "Error: " + e.getMessage(), 30);
        }
    }

    // Message send commands

    public static void sendMessage(GuildMessageChannel channel, String content, int deleteAfter) {
        channel.sendMessage(content)
                .queue(m -> m.delete().queueAfter(deleteAfter, TimeUnit.SECONDS));
    }

    public static void sendMessageTranslate(GuildMessageChannel channel, String content, int deleteAfter) {
        channel.sendMessage(LocaleManager.format(channel.getGuild(), content))
                .queue(m -> m.delete().queueAfter(deleteAfter, TimeUnit.SECONDS));
    }

    public static void replyMessage(Message message, String content, int deleteAfter) {
        message.reply(content)
                .queue(m -> m.delete().queueAfter(deleteAfter, TimeUnit.SECONDS));
    }

    public static void replyMessageTranslate(Message message, String content, int deleteAfter) {
        message.reply(LocaleManager.format(message.getGuild(),
                content))
                .queue(m -> m.delete().queueAfter(deleteAfter, TimeUnit.SECONDS));
    }

    public static void reply(InteractionHook hook, String content, int deleteAfter) {
        hook.sendMessage(content)
                .queue(_message -> _message.delete().queueAfter(deleteAfter, TimeUnit.SECONDS));
    }

    public static void replyTranslate(InteractionHook hook, String content, int deleteAfter) {
        hook.sendMessage(LocaleManager.format(hook.getInteraction().getGuild(), content))
                .queue(_message -> _message.delete().queueAfter(deleteAfter, TimeUnit.SECONDS));
    }

    public static void replyEmbed(InteractionHook hook, EmbedBuilder builder, int deleteAfter) {
        hook.sendMessageEmbeds(builder.build())
                .queue(_message -> _message.delete().queueAfter(deleteAfter, TimeUnit.SECONDS));
    }

    public static void replyEmbedTranslate(InteractionHook hook, EmbedBuilder builder, int deleteAfter) {
        hook.sendMessageEmbeds(getFormatEmbedBuilder(hook.getInteraction().getGuild(), builder).build())
                .queue(_message -> _message.delete().queueAfter(deleteAfter, TimeUnit.SECONDS));
    }

    public static void replyEmbed(InteractionHook hook, String content, int deleteAfter) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.addField(EmbedBuilder.ZERO_WIDTH_SPACE, content, false);
        hook.sendMessageEmbeds(builder.build())
                .queue(_message -> _message.delete().queueAfter(deleteAfter, TimeUnit.SECONDS));
    }

    public static EmbedBuilder getFormatEmbedBuilder(Guild guild, EmbedBuilder builder) {
        List<Field> fields = new LinkedList<>(builder.getFields());
        builder.clearFields();
        builder.setDescription(LocaleManager.format(guild, builder.getDescriptionBuilder().toString()));

        for (Field field : fields) {

            String name = LocaleManager.format(guild, field.getName());
            String value = LocaleManager.format(guild, field.getValue());

            builder.addField(name, value, field.isInline());
        }
        return builder;
    }

    public static void delete(InteractionHook hook) {
        hook.deleteOriginal().queue();
    }
}
