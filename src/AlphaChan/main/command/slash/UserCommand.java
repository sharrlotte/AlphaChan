package AlphaChan.main.command.slash;

import AlphaChan.main.command.SimpleBotCommand;
import AlphaChan.main.command.slash.subcommands.user.AvatarCommand;
import AlphaChan.main.command.slash.subcommands.user.DailyCommand;
import AlphaChan.main.command.slash.subcommands.user.DeletedMessageCommand;
import AlphaChan.main.command.slash.subcommands.user.InfoCommand;
import AlphaChan.main.command.slash.subcommands.user.LeaderboardCommand;
import AlphaChan.main.command.slash.subcommands.user.SetNicknameCommand;
import AlphaChan.main.command.slash.subcommands.user.ShowLevelCommand;
import AlphaChan.main.command.slash.subcommands.user.TransferCommand;

public class UserCommand extends SimpleBotCommand {
    public UserCommand() {
        super("user", "Lệnh liên quan đến người dùng");
        addSubcommands(new DailyCommand());
        addSubcommands(new ShowLevelCommand());
        addSubcommands(new InfoCommand());
        addSubcommands(new LeaderboardCommand());
        addSubcommands(new SetNicknameCommand());
        addSubcommands(new TransferCommand());
        addSubcommands(new AvatarCommand());
        addSubcommands(new DeletedMessageCommand());
    }

}
