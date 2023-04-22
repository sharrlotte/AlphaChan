package alpha.main.command.slash.subcommands.yui;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static alpha.main.AlphaChan.*;

import java.util.HashMap;
import java.util.List;

import alpha.main.command.SlashCommand;
import alpha.main.command.SlashSubcommand;

public class SayCommand extends SlashSubcommand {

    public SayCommand() {
        super("say", "Yui only");
        addCommandOption(OptionType.STRING, "content", "Yui only", true);
        addCommandOption(OptionType.STRING, "guild", "Yui only", false, true);
        addCommandOption(OptionType.STRING, "channel", "Yui only", false, true);
        addCommandOption(OptionType.STRING, "reply", "Yui only", false, true);

    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        OptionMapping guildIdOption = event.getOption("guild");
        OptionMapping channelIdOption = event.getOption("channel");
        OptionMapping contentOption = event.getOption("content");
        OptionMapping replyIdOption = event.getOption("reply");
        event.getHook().deleteOriginal().complete();

        if (contentOption == null)
            return;

        String content = contentOption.getAsString();
        Guild guild;
        MessageChannelUnion channel;
        if (guildIdOption == null)
            guild = event.getGuild();
        else
            guild = jda.getGuildById(guildIdOption.getAsString());

        if (guild == null || channelIdOption == null)
            channel = event.getChannel();
        else
            channel = (MessageChannelUnion) guild.getTextChannelById(channelIdOption.getAsString());

        if (channel == null)
            channel = event.getChannel();

        if (replyIdOption == null)
            channel.sendMessage(content).queue();
        else {
            channel.retrieveMessageById(replyIdOption.getAsString()).queue((message) -> message.reply(content));
        }

    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        String focus = event.getFocusedOption().getName();
        if (focus.equals("guild")) {
            HashMap<String, String> guildNames = new HashMap<>();
            jda.getGuilds().forEach(s -> guildNames.put(s.getName(), s.getId()));
            SlashCommand.sendAutoComplete(event, guildNames);

            // Show all channels
        } else if (focus.equals("channel")) {
            // Get all channel form selected guild
            Guild guild;
            OptionMapping guildIdOption = event.getOption("guild");
            if (guildIdOption == null)
                guild = event.getGuild();
            else
                guild = jda.getGuildById(guildIdOption.getAsString());

            if (guild == null) {
                SlashCommand.sendAutoComplete(event, "No guild found");
                return;
            }

            List<TextChannel> channels = guild.getTextChannels();
            HashMap<String, String> channelNames = new HashMap<>();
            channels.forEach(c -> channelNames.put(c.getName(), c.getId()));
            SlashCommand.sendAutoComplete(event, channelNames);

        } else if (focus.equals("reply")) {
            OptionMapping guildIdOption = event.getOption("guild");
            OptionMapping channelIdOption = event.getOption("channel");

            Guild guild;
            MessageChannelUnion channel;
            if (guildIdOption == null)
                guild = event.getGuild();
            else
                guild = jda.getGuildById(guildIdOption.getAsString());

            if (guild == null || channelIdOption == null)
                channel = event.getChannel();
            else
                channel = (MessageChannelUnion) guild.getTextChannelById(channelIdOption.getAsString());

            if (channel == null)
                channel = event.getChannel();

            channel.getHistory().retrieveFuture(10).queue(messages -> {
                HashMap<String, String> messageContents = new HashMap<>();
                messages.forEach(m -> messageContents.put(m.getContentDisplay(), m.getId()));
                SlashCommand.sendAutoComplete(event, messageContents);
            });
        }
    }
}
