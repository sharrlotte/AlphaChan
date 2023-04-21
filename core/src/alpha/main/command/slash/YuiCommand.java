package alpha.main.command.slash;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import alpha.main.command.SlashCommand;
import alpha.main.command.slash.subcommands.yui.AddCommand;
import alpha.main.command.slash.subcommands.yui.SayCommand;
import alpha.main.command.slash.subcommands.yui.SetRoleCommand;
import alpha.main.command.slash.subcommands.yui.UpdateCommand;
import alpha.main.handler.MessageHandler;
import alpha.main.handler.UserHandler;
import alpha.main.util.StringUtils;

public class YuiCommand extends SlashCommand {

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
            MessageHandler.replyTranslate(event.getHook(),
                    StringUtils.backtick("<command.no_permission>[You don't have permission to use this command]"), 10);
            return;
        }
        runCommand(event);
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {

        if (!UserHandler.isYui(event.getMember())) {
            sendAutoComplete(event, "<command.no_permission>[You don't have permission to use this command]");
            return;
        }
        super.onAutoComplete(event);
    }
}
