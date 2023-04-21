package alpha.main.command.slash;

import alpha.main.command.SlashCommand;
import alpha.main.command.slash.subcommands.bot.AskCommand;
import alpha.main.command.slash.subcommands.bot.GuildCommand;
import alpha.main.command.slash.subcommands.bot.HelpCommand;
import alpha.main.command.slash.subcommands.bot.InfoCommand;
import alpha.main.command.slash.subcommands.bot.PingCommand;

public class BotCommand extends SlashCommand {
    public BotCommand() {
        super("bot", "<command.command_bot>[Commands for everyone]");
        addSubcommands(new InfoCommand());
        addSubcommands(new GuildCommand());
        addSubcommands(new HelpCommand());
        addSubcommands(new PingCommand());
        addSubcommands(new AskCommand());
    }
}
