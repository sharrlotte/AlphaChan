package AlphaChan.main.command;

import AlphaChan.main.command.subcommands.MindustryCommands.PingCommand;
import AlphaChan.main.command.subcommands.MindustryCommands.PostSchemCommand;
import AlphaChan.main.command.subcommands.MindustryCommands.RefreshServerCommand;
import AlphaChan.main.command.subcommands.MindustryCommands.SearchSchematicCommand;
import AlphaChan.main.util.SimpleBotCommand;

public class MindustryCommand extends SimpleBotCommand {
    public MindustryCommand() {
        super("mindustry", "Các lệnh liên quan đến mindustry");
        addSubcommands(new PingCommand());
        addSubcommands(new PostSchemCommand());
        addSubcommands(new RefreshServerCommand());
        addSubcommands(new SearchSchematicCommand());
    }
}
