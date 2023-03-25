package AlphaChan.main.command.console;

import AlphaChan.main.command.ConsoleCommandEvent;
import AlphaChan.main.command.ConsoleCommand;
import AlphaChan.main.handler.CommandHandler;
import AlphaChan.main.util.Log;

public class ReloadSlashCommandConsole extends ConsoleCommand {

    public ReloadSlashCommandConsole() {
        super("reload-slash-command", "\n\t- <>: Reload bot slash command");
    }

    @Override
    public void runCommand(ConsoleCommandEvent command) {
        Log.system("Reload slash command");
        CommandHandler.updateCommand();
        Log.system("Slash command reloaded");
    }
}
