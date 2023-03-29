package AlphaChan.main.command.slash;

import AlphaChan.main.command.SlashCommand;
import AlphaChan.main.command.slash.subcommands.bot.AskCommand;
import AlphaChan.main.command.slash.subcommands.bot.GuildCommand;
import AlphaChan.main.command.slash.subcommands.bot.HelpCommand;
import AlphaChan.main.command.slash.subcommands.bot.InfoCommand;
import AlphaChan.main.command.slash.subcommands.bot.PingCommand;

public class BotCommand extends SlashCommand {
    public BotCommand() {
        super("bot", "<?command.command_bot>");
        addSubcommands(new InfoCommand());
        addSubcommands(new GuildCommand());
        addSubcommands(new HelpCommand());
        addSubcommands(new PingCommand());
        addSubcommands(new AskCommand());
    }
}
