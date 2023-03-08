package AlphaChan.main.console;

import AlphaChan.main.handler.UpdatableHandler;
import AlphaChan.main.util.ConsoleCommand;
import AlphaChan.main.util.Log;
import AlphaChan.main.util.SimpleConsoleCommand;

public class ReloadSlashCommandConsole extends SimpleConsoleCommand {

    public ReloadSlashCommandConsole() {
        super("reload-slash-command", "\n\t- <>: Reload bot slash command");
    }

    @Override
    public void runCommand(ConsoleCommand command) {
        Log.system("Reload slash command");
        UpdatableHandler.updateCommand();
        Log.system("Slash command reloaded");
    }
}
