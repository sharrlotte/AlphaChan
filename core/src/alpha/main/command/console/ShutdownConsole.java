package alpha.main.command.console;

import alpha.main.command.ConsoleCommandEvent;
import alpha.main.AlphaChan;
import alpha.main.command.ConsoleCommand;

public class ShutdownConsole extends ConsoleCommand {

    public ShutdownConsole() {
        super("shutdown", "\n\t- Shutdown the bot");
    }

    @Override
    public void runCommand(ConsoleCommandEvent event) {
        AlphaChan.shutdown();
    }
}
