package AlphaChan.main.command.slash.subcommands.admin;

import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.HashMap;

import AlphaChan.main.command.SlashSubcommand;
import AlphaChan.main.data.user.GuildCache;
import AlphaChan.main.data.user.GuildCache.ChannelType;
import AlphaChan.main.handler.GuildHandler;

public class SetChannelCommand extends SlashSubcommand {

    public SetChannelCommand() {
        super("setchannel", "Cài đặt các kênh của máy chủ");
        this.addOption(OptionType.STRING, "type", "Loại kênh muốn đặt", true, true);
    }

    @Override
    public String getHelpString() {
        return "Cài đặt các kênh của máy chủ:\n\t<type>: loại kênh muốn đặt\n\tThêm lần nữa để xóa";
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
                    reply(event, "Xóa kênh thành công", 30);
                else
                    reply(event, "Xóa kênh không thành công", 30);

            } else {
                if (guildData.addChannel(type, channel.getId()))
                    reply(event, "Thêm kênh thành công", 30);
                else
                    reply(event, "Thêm kênh không thành công", 30);
            }
        } catch (Exception e) {
            reply(event, "Loại kênh không hợp lệ", 10);
        }
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        String focus = event.getFocusedOption().getName();
        if (focus.equals("type")) {
            HashMap<String, String> options = new HashMap<String, String>();
            for (ChannelType t : ChannelType.values())
                options.put(t.name(), t.name());
            sendAutoComplete(event, options);
        }
    }

}
