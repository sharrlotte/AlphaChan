package AlphaChan.main.command;

import AlphaChan.main.command.subcommands.UserCommands.AvatarCommand;
import AlphaChan.main.command.subcommands.UserCommands.DailyCommand;
import AlphaChan.main.command.subcommands.UserCommands.DeletedMessageCommand;
import AlphaChan.main.command.subcommands.UserCommands.InfoCommand;
import AlphaChan.main.command.subcommands.UserCommands.LeaderboardCommand;
import AlphaChan.main.command.subcommands.UserCommands.SetNicknameCommand;
import AlphaChan.main.command.subcommands.UserCommands.ShowLevelCommand;
import AlphaChan.main.command.subcommands.UserCommands.TransferCommand;
import AlphaChan.main.util.SimpleBotCommand;

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
