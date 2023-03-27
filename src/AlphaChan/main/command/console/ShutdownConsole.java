package AlphaChan.main.command.console;

import AlphaChan.main.command.ConsoleCommandEvent;
import AlphaChan.AlphaChan;
import AlphaChan.main.command.ConsoleCommand;

public class ShutdownConsole extends ConsoleCommand {

    public ShutdownConsole() {
        super("shutdown", "\n\t- Shutdown the bot");
    }

    @Override
    public void runCommand(ConsoleCommandEvent command) {
        AlphaChan.shutdown();
    }
}
