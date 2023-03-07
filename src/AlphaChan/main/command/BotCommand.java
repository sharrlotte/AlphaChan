package AlphaChan.main.command;

import AlphaChan.main.command.subcommands.BotCommands.GuildCommand;
import AlphaChan.main.command.subcommands.BotCommands.HelpCommand;
import AlphaChan.main.command.subcommands.BotCommands.InfoCommand;
import AlphaChan.main.util.SimpleBotCommand;

public class BotCommand extends SimpleBotCommand {
    public BotCommand() {
        super("bot", "Các lệnh liên quan đến bot");
        addSubcommands(new InfoCommand());
        addSubcommands(new GuildCommand());
        addSubcommands(new HelpCommand());
    }

}
