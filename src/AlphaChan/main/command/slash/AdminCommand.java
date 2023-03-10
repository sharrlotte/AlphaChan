package AlphaChan.main.command.slash;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import AlphaChan.main.command.SimpleBotCommand;
import AlphaChan.main.command.slash.subcommands.admin.GuildShowLevelCommand;
import AlphaChan.main.command.slash.subcommands.admin.RefreshSlashCommand;
import AlphaChan.main.command.slash.subcommands.admin.ReloadServerCommand;
import AlphaChan.main.command.slash.subcommands.admin.SetAdminCommand;
import AlphaChan.main.command.slash.subcommands.admin.SetChannelCommand;
import AlphaChan.main.command.slash.subcommands.admin.SetLevelRoleCommand;
import AlphaChan.main.handler.UserHandler;

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
