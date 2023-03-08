package AlphaChan.main.handler;

import java.util.Collection;
import java.util.HashMap;

import AlphaChan.main.console.HelpConsole;
import AlphaChan.main.console.ReloadConfigConsole;
import AlphaChan.main.console.ShowGuildConsole;
import AlphaChan.main.console.ShowUserConsole;
import AlphaChan.main.util.ConsoleCommand;
import AlphaChan.main.util.Log;
import AlphaChan.main.util.SimpleConsoleCommand;

public class ConsoleHandler {

    private static ConsoleHandler instance = new ConsoleHandler();
    private static boolean isRunning = true;

    private static String command = new String();
    private static final String SEPARATOR = " ";

    private static HashMap<String, SimpleConsoleCommand> commands;

    private ConsoleHandler() {
        commands = new HashMap<>();

        addCommand(new ShowGuildConsole());
        addCommand(new ShowUserConsole());
        addCommand(new HelpConsole());
        addCommand(new ReloadConfigConsole());

        UpdatableHandler.run("CONSOLE", 0, () -> run());
    }

    public static ConsoleHandler getInstance() {
        if (instance == null)
            instance = new ConsoleHandler();
        return instance;
    }

    public static Collection<String> getCommands() {
        return commands.keySet();
    }

    public static SimpleConsoleCommand getCommand(String name) {
        return commands.get(name);
    }

    public static void addCommand(SimpleConsoleCommand command) {
        commands.put(command.getName(), command);
    }

    public static void run() {
        isRunning = true;
        while (isRunning) {
            command = System.console().readLine();
            runCommand(command);
        }
    }

    public static void kill() {
        isRunning = false;
    }

    private static void runCommand(String command) {

        if (command == null)
            return;

        if (command.isBlank())
            return;

        if (command.charAt(0) != '/')
            return;

        command = command.substring(1, command.length());
        String[] field = command.split(SEPARATOR);

        if (commands.containsKey(field[0])) {
            commands.get(field[0]).onCommand(new ConsoleCommand(command));

        } else {
            int maxPoint = 0, point = 0;
            String estimate = new String();

            for (String c : commands.keySet()) {

                int length = Math.min(command.length(), c.length());
                for (int i = 0; i < length; i++) {
                    if (c.charAt(i) == command.charAt(i))
                        point++;
                }

                if (point > maxPoint) {
                    estimate = c;
                    maxPoint = point;
                }
                point = 0;
            }

            if (maxPoint >= (int) (command.length() / 2)) {
                Log.info("COMMAND NOT FOUND", "[/" + command + "] doesn't exists, do you mean [/" + estimate + "]");
            } else {
                Log.info("COMMAND NOT FOUND", "[/" + command + "] doesn't exists, use [/help] to get all command");
            }
        }
    }
}
