package AlphaChan.main.command.slash;

import AlphaChan.main.command.SlashCommand;
import AlphaChan.main.command.slash.subcommands.mindustry.CalculatorCommand;
import AlphaChan.main.command.slash.subcommands.mindustry.PingCommand;
import AlphaChan.main.command.slash.subcommands.mindustry.PostSchemCommand;
import AlphaChan.main.command.slash.subcommands.mindustry.RefreshServerCommand;
import AlphaChan.main.command.slash.subcommands.mindustry.SearchSchematicCommand;

public class MindustryCommand extends SlashCommand {
    public MindustryCommand() {
        super("mindustry", "Các lệnh liên quan đến mindustry");
        addSubcommands(new PingCommand());
        addSubcommands(new PostSchemCommand());
        addSubcommands(new RefreshServerCommand());
        addSubcommands(new SearchSchematicCommand());
        addSubcommands(new CalculatorCommand());
    }
}
