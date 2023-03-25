package AlphaChan.main.command.console;

import AlphaChan.BotConfig;
import AlphaChan.main.command.ConsoleCommandEvent;
import AlphaChan.main.command.ConsoleCommand;
import AlphaChan.main.util.Log;

public class ReloadConfigConsole extends ConsoleCommand {

    public ReloadConfigConsole() {
        super("reload-config", "\n\t- <> Reload config file");
    }

    @Override
    public void runCommand(ConsoleCommandEvent command) {
        BotConfig.load();
        Log.system("Bot config: " + BotConfig.getProperties().toString());
        
    }
}
