package AlphaChan.main.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import AlphaChan.main.command.subcommands.AdminCommands.GuildShowLevelCommand;
import AlphaChan.main.command.subcommands.AdminCommands.RefreshSlashCommand;
import AlphaChan.main.command.subcommands.AdminCommands.ReloadServerCommand;
import AlphaChan.main.command.subcommands.AdminCommands.SetAdminCommand;
import AlphaChan.main.command.subcommands.AdminCommands.SetChannelCommand;
import AlphaChan.main.command.subcommands.AdminCommands.SetLevelRoleCommand;
import AlphaChan.main.handler.UserHandler;
import AlphaChan.main.util.SimpleBotCommand;

public class AdminCommand extends SimpleBotCommand {

    public AdminCommand() {
        super("admin", "Admin only");
        addSubcommands(new RefreshSlashCommand());
        addSubcommands(new ReloadServerCommand());
        addSubcommands(new SetChannelCommand());
        addSubcommands(new SetLevelRoleCommand());
        addSubcommands(new GuildShowLevelCommand());
        addSubcommands(new SetAdminCommand());
    }

    @Override
    public String getHelpString() {
        return "Lệnh dành riêng cho admin";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        if (UserHandler.isAdmin(event.getMember()))
            runCommand(event);
        else
            reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);

    }
}
