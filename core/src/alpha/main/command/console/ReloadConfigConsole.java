package alpha.main.command.console;

import alpha.main.command.ConsoleCommandEvent;
import alpha.main.BotConfig;
import alpha.main.command.ConsoleCommand;
import alpha.main.util.Log;

public class ReloadConfigConsole extends ConsoleCommand {

    public ReloadConfigConsole() {
        super("reload-config", "\n\t- <> Reload config file");
    }

    @Override
    public void onCommand(ConsoleCommandEvent event) {
        BotConfig.load();
        Log.system("Bot config: " + BotConfig.getProperties().toString());

    }
}
