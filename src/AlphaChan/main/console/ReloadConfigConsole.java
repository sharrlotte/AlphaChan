package AlphaChan.main.console;

import AlphaChan.BotConfig;
import AlphaChan.main.util.ConsoleCommand;
import AlphaChan.main.util.Log;
import AlphaChan.main.util.SimpleConsoleCommand;

public class ReloadConfigConsole extends SimpleConsoleCommand {

    public ReloadConfigConsole() {
        super("reload-config", "\n\t- <> Reload config file");
    }

    @Override
    public void runCommand(ConsoleCommand command) {
        BotConfig.load();
        Log.system("Bot config: " + BotConfig.getProperties().toString());
    }
}
