package alpha.main.command.slash;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import alpha.main.command.SlashCommand;
import alpha.main.command.slash.subcommands.admin.SetAdminCommand;
import alpha.main.command.slash.subcommands.admin.SetChannelCommand;
import alpha.main.command.slash.subcommands.admin.SetLevelRoleCommand;
import alpha.main.handler.MessageHandler;
import alpha.main.handler.UserHandler;

public class AdminCommand extends SlashCommand {

    public AdminCommand() {
        super("admin", "<command.command_admin>[Commands for admin]");
        addSubcommands(new SetChannelCommand());
        addSubcommands(new SetLevelRoleCommand());
        addSubcommands(new SetAdminCommand());
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        if (UserHandler.isAdmin(event.getMember()))
            runCommand(event);
        else
            MessageHandler.replyTranslate(event.getHook(),
                    "<command.no_permission>[You don't have permission to use this command]", 10);
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        if (UserHandler.isAdmin(event.getMember()))
            super.onAutoComplete(event);
        else
            sendAutoComplete(event, "<command.no_permission>[You don't have permission to use this command]");
    }
}
