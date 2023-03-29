package AlphaChan.main.command.slash.subcommands.admin;

import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.HashMap;

import AlphaChan.main.command.SlashCommand;
import AlphaChan.main.command.SlashSubcommand;
import AlphaChan.main.data.user.GuildCache;
import AlphaChan.main.data.user.GuildCache.ChannelType;
import AlphaChan.main.handler.GuildHandler;
import AlphaChan.main.handler.MessageHandler;

public class SetChannelCommand extends SlashSubcommand {

    public SetChannelCommand() {
        super("setchannel", "<@command.command_set_channel>");
        this.addOption(OptionType.STRING, "type", "<@command.channel_type>", true, true);
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        try {
            OptionMapping typeOption = event.getOption("type");
            if (typeOption == null)
                throw new IllegalStateException("Invalid option");

            ChannelType type = ChannelType.valueOf(typeOption.getAsString());

            MessageChannelUnion channel = event.getChannel();
            GuildCache guildData = GuildHandler.getGuild(event.getGuild());
            if (guildData == null)
                throw new IllegalStateException("Guild data not found with <" + event.getGuild() + ">");

            if (guildData.hasChannel(type, channel.getId())) {
                if (guildData.removeChannel(type, channel.getId()))
                    MessageHandler.reply(event, "<@command.delete_failed>", 30);
                else
                    MessageHandler.reply(event, "<@command.delete_successfully>", 30);

            } else {
                if (guildData.addChannel(type, channel.getId()))
                    MessageHandler.reply(event, "<@command.add_successfully>", 30);
                else
                    MessageHandler.reply(event, "<@command.add_failed>", 30);
            }
        } catch (Exception e) {
            MessageHandler.reply(event, "Loại kênh không hợp lệ", 10);
        }
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        String focus = event.getFocusedOption().getName();
        if (focus.equals("type")) {
            HashMap<String, String> options = new HashMap<String, String>();
            for (ChannelType t : ChannelType.values())
                options.put(t.name(), t.name());
            SlashCommand.sendAutoComplete(event, options);
        }
    }

}
