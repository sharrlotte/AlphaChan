package AlphaChan.main.command.console;

import AlphaChan.main.command.ConsoleCommandEvent;
import AlphaChan.main.command.SimpleConsoleCommand;

public class ShutdownConsole extends SimpleConsoleCommand {

    public ShutdownConsole() {
        super("shutdown", "\n\t- Shutdown the bot");
    }

    @Override
    public void runCommand(ConsoleCommandEvent command) {
        System.exit(0);
    }

}
