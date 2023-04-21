package alpha.main.command.console;

import alpha.main.command.ConsoleCommandEvent;
import alpha.main.command.ConsoleCommand;

public class HelpConsole extends ConsoleCommand {

    public HelpConsole() {
        super("help", "\n\t- <>: Help command" + //
                "\n\t- <command name>: Command name to show");
    }

    @Override
    public void runCommand(ConsoleCommandEvent event) {

        // if (command.getArgumentCount() == 0) {
        // StringBuilder content = new StringBuilder();
        // ArrayList<String> commands = new ArrayList<>();
        // commands.addAll(ConsoleCommandHandler.getCommands());

        // commands.sort(new Comparator<String>() {
        // public int compare(String s1, String s2) {
        // int length = s1.length() < s2.length() ? s1.length() : s2.length();
        // for (int i = 0; i < length; i++) {
        // if (s1.charAt(i) < s2.charAt(i))
        // return -1;
        // if (s1.charAt(i) < s2.charAt(i))
        // return 1;
        // }
        // return 0;
        // }
        // });

        // for (String c : commands) {
        // content.append("\n\t- " + c);
        // }

        // Log.info("COMMAND LIST", content.toString());

        // } else if (command.getArgumentCount() == 1) {
        // String commandName = command.nextString();

        // ConsoleCommand cc = ConsoleCommandHandler.getCommand(commandName);
        // if (cc == null)
        // Log.warning("Command with name " + commandName + " doesn't exists");
        // else
        // Log.info(commandName, "" + cc.getDescription());

        // } else {
        // Log.error("Command require [0-1] arguments ");
        // }
    }
}
