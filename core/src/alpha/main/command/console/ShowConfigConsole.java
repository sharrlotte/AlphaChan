package alpha.main.command.console;

import alpha.main.command.ConsoleCommandEvent;
import alpha.main.BotConfig;
import alpha.main.command.ConsoleCommand;
import alpha.main.util.Log;
import alpha.main.util.StringUtils;

public class ShowConfigConsole extends ConsoleCommand {

    public ShowConfigConsole() {
        super("show-config", "\n\t- <> Show bot configures");
    }

    @Override
    public void runCommand(ConsoleCommandEvent event) {
        Log.system("\nBot config: " + StringUtils.mapToLines(BotConfig.getProperties()));

    }
}
