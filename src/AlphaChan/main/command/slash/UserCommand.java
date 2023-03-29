package AlphaChan.main.command.slash;

import AlphaChan.main.command.SlashCommand;
import AlphaChan.main.command.slash.subcommands.user.AvatarCommand;
import AlphaChan.main.command.slash.subcommands.user.DailyCommand;
import AlphaChan.main.command.slash.subcommands.user.DeletedMessageCommand;
import AlphaChan.main.command.slash.subcommands.user.InfoCommand;
import AlphaChan.main.command.slash.subcommands.user.LeaderboardCommand;
import AlphaChan.main.command.slash.subcommands.user.TransferCommand;

public class UserCommand extends SlashCommand {
    public UserCommand() {
        super("user", "<@command.command_user>");
        addSubcommands(new DailyCommand());
        addSubcommands(new InfoCommand());
        addSubcommands(new LeaderboardCommand());
        addSubcommands(new TransferCommand());
        addSubcommands(new AvatarCommand());
        addSubcommands(new DeletedMessageCommand());
    }

}
