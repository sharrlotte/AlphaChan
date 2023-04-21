package alpha.main.command.slash;

import alpha.main.command.SlashCommand;
import alpha.main.command.slash.subcommands.mindustry.PingCommand;
import alpha.main.command.slash.subcommands.mindustry.PostSchemCommand;
import alpha.main.command.slash.subcommands.mindustry.RefreshServerCommand;
import alpha.main.command.slash.subcommands.mindustry.SearchSchematicCommand;

public class MindustryCommand extends SlashCommand {
    public MindustryCommand() {
        super("mindustry", "<command.command_mindustry>[Commands for everyone]");
        addSubcommands(new PingCommand());
        addSubcommands(new PostSchemCommand());
        addSubcommands(new RefreshServerCommand());
        addSubcommands(new SearchSchematicCommand());
    }
}
