package AlphaChan.main.console;

import java.util.ArrayList;
import java.util.Comparator;

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
            ArrayList<String> commands = new ArrayList<>();
            commands.addAll(ConsoleHandler.getCommands());

            commands.sort(new Comparator<String>() {
                public int compare(String s1, String s2) {
                    int length = s1.length() < s2.length() ? s1.length() : s2.length();
                    for (int i = 0; i < length; i++) {
                        if (s1.charAt(i) < s2.charAt(i))
                            return -1;
                        if (s1.charAt(i) < s2.charAt(i))
                            return 1;
                    }
                    return 0;
                }
            });

            for (String c : commands) {
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
