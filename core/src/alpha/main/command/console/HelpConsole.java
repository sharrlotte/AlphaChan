package alpha.main.command.console;

import alpha.main.command.ConsoleCommandEvent;
import alpha.main.command.ConsoleCommandOptionData.OptionType;
import alpha.main.handler.CommandHandler.ConsoleCommandHandler;
import alpha.main.util.Log;

import java.util.ArrayList;
import java.util.Comparator;

import alpha.main.command.ConsoleCommand;

public class HelpConsole extends ConsoleCommand {

    public HelpConsole() {
        super("help", "Show help command");
        addCommandOption("command", OptionType.STRING, "Command name to show");
    }

    @Override
    public void onCommand(ConsoleCommandEvent event) {

        String commandName = event.getOption("command");

        if (commandName == null) {
            StringBuilder content = new StringBuilder();
            ArrayList<String> commands = new ArrayList<>(ConsoleCommandHandler.getCommands());

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

        } else {
            ConsoleCommand cc = ConsoleCommandHandler.getCommand(commandName);
            if (cc == null)
                Log.warning("Command with name " + commandName + " doesn't exists");
            else
                Log.info(commandName, "" + cc.getDescription());

        }
    }
}
