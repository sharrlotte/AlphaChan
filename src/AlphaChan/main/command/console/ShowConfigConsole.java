package AlphaChan.main.command.console;

import AlphaChan.BotConfig;
import AlphaChan.main.command.ConsoleCommandEvent;
import AlphaChan.main.command.SimpleConsoleCommand;
import AlphaChan.main.util.Log;

public class ShowConfigConsole extends SimpleConsoleCommand {

    public ShowConfigConsole() {
        super("show-config", "\n\t- <> Show bot configures");
    }

    @Override
    public void runCommand(ConsoleCommandEvent command) {
        Log.system("Bot config: " + BotConfig.getProperties().toString());

    }
}
