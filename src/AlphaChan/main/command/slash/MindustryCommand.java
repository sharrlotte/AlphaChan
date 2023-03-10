package AlphaChan.main.command.slash;

import AlphaChan.main.command.SimpleBotCommand;
import AlphaChan.main.command.slash.subcommands.mindustry.PingCommand;
import AlphaChan.main.command.slash.subcommands.mindustry.PostSchemCommand;
import AlphaChan.main.command.slash.subcommands.mindustry.RefreshServerCommand;
import AlphaChan.main.command.slash.subcommands.mindustry.SearchSchematicCommand;

public class MindustryCommand extends SimpleBotCommand {
    public MindustryCommand() {
        super("mindustry", "Các lệnh liên quan đến mindustry");
        addSubcommands(new PingCommand());
        addSubcommands(new PostSchemCommand());
        addSubcommands(new RefreshServerCommand());
        addSubcommands(new SearchSchematicCommand());
    }
}
