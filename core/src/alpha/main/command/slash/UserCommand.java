package alpha.main.command.slash;

import alpha.main.command.SlashCommand;
import alpha.main.command.slash.subcommands.user.AvatarCommand;
import alpha.main.command.slash.subcommands.user.DailyCommand;
import alpha.main.command.slash.subcommands.user.DeletedMessageCommand;
import alpha.main.command.slash.subcommands.user.InfoCommand;
import alpha.main.command.slash.subcommands.user.LeaderboardCommand;
import alpha.main.command.slash.subcommands.user.TransferCommand;

public class UserCommand extends SlashCommand {
    public UserCommand() {
        super("user", "<command.command_user>[Commands for everyone]");
        addSubcommands(new DailyCommand());
        addSubcommands(new InfoCommand());
        addSubcommands(new LeaderboardCommand());
        addSubcommands(new TransferCommand());
        addSubcommands(new AvatarCommand());
        addSubcommands(new DeletedMessageCommand());
    }

}
