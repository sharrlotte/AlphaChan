package AlphaChan.main.command.slash;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import AlphaChan.main.command.SlashCommand;
import AlphaChan.main.command.slash.subcommands.admin.ReloadServerCommand;
import AlphaChan.main.command.slash.subcommands.admin.SetAdminCommand;
import AlphaChan.main.command.slash.subcommands.admin.SetChannelCommand;
import AlphaChan.main.command.slash.subcommands.admin.SetLevelRoleCommand;
import AlphaChan.main.handler.MessageHandler;
import AlphaChan.main.handler.UserHandler;

public class AdminCommand extends SlashCommand {

    public AdminCommand() {
        super("admin", "<@command.command_admin>");
        addSubcommands(new ReloadServerCommand());
        addSubcommands(new SetChannelCommand());
        addSubcommands(new SetLevelRoleCommand());
        addSubcommands(new SetAdminCommand());
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        if (UserHandler.isAdmin(event.getMember()))
            runCommand(event);
        else
            MessageHandler.reply(event, "<@command.no_permission>", 10);
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        if (UserHandler.isAdmin(event.getMember()))
            super.onAutoComplete(event);
        else
            sendAutoComplete(event, "Bạn không có quyền để sử dụng lệnh này");
    }
}
