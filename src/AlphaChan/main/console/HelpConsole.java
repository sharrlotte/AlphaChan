package AlphaChan.main.console;

import AlphaChan.main.handler.ConsoleHandler;
import AlphaChan.main.util.ConsoleCommand;
import AlphaChan.main.util.Log;
import AlphaChan.main.util.SimpleConsoleCommand;

public class HelpConsole extends SimpleConsoleCommand {

    public HelpConsole() {
        super("help", "\n\t- <>: Show all commands" + //
                "\n\t- <command name>: Show command");
    }

    @Override
    public void runCommand(ConsoleCommand command) {

        if (command.getArgumentCount() == 0) {
            StringBuilder content = new StringBuilder();
            for (String c : ConsoleHandler.getCommands()) {
                content.append("\n\t- " + c);
            }

            Log.info("COMMAND LIST", content.toString());
        } else if (command.getArgumentCount() == 1) {
            String commandName = command.nextString();

            SimpleConsoleCommand cc = ConsoleHandler.getCommand(commandName);
            if (cc == null)
                Log.warning("Command with name " + commandName + " doesn't exists");
            else
                Log.info(commandName, "" + cc.getDescription());

        } else {
            Log.error("Command require [0-1] arguments ");
        }
    }
}
