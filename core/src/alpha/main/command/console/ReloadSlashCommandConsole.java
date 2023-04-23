package alpha.main.command.console;

import alpha.main.command.ConsoleCommandEvent;
import alpha.main.command.ConsoleCommand;
import alpha.main.handler.CommandHandler;
import alpha.main.util.Log;

public class ReloadSlashCommandConsole extends ConsoleCommand {

    public ReloadSlashCommandConsole() {
        super("reload-slash-command", "Reload bot slash command");
    }

    @Override
    public void onCommand(ConsoleCommandEvent event) {
        Log.system("Reload slash command");
        CommandHandler.updateCommand();
        Log.system("Slash command reloaded");
    }
}
