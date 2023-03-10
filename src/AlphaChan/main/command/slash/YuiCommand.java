package AlphaChan.main.command.slash;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import AlphaChan.main.command.SimpleBotCommand;
import AlphaChan.main.command.slash.subcommands.yui.AddCommand;
import AlphaChan.main.command.slash.subcommands.yui.SayCommand;
import AlphaChan.main.command.slash.subcommands.yui.SetRoleCommand;
import AlphaChan.main.command.slash.subcommands.yui.UpdateCommand;
import AlphaChan.main.handler.UserHandler;

public class YuiCommand extends SimpleBotCommand {

    public YuiCommand() {
        super("yui", "Yui only");
        addSubcommands(new AddCommand());
        addSubcommands(new SayCommand());
        addSubcommands(new SetRoleCommand());
        addSubcommands(new UpdateCommand());
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        if (!UserHandler.isYui(event.getMember())) {
            reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);
            return;
        }
        runCommand(event);
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {

        if (!UserHandler.isYui(event.getMember())) {
            sendAutoComplete(event, "Bạn không có quyền để sử dụng lệnh này");
            return;
        }
        if (subcommands.containsKey(event.getSubcommandName())) {
            subcommands.get(event.getSubcommandName()).onAutoComplete(event);
        }
    }
}
