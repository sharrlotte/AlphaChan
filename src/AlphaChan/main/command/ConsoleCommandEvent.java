package AlphaChan.main.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConsoleCommandEvent {

    private final String name;
    private final HashMap<String, String> optionMap;

    private ConsoleCommandEvent(String name, HashMap<String, String> optionMap) {
        this.name = name;
        this.optionMap = optionMap;
    }

    public static ConsoleCommandEvent parseCommand(String command) {
        List<String> arguments = new ArrayList<>();
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < command.length(); i++) {

            if (command.charAt(i) == '{') {
                i++;
                while (i < command.length() && command.charAt(i) != '}') {
                    string.append(command.charAt(i));
                    i++;
                }
                arguments.add(string.toString());
                string.delete(0, string.length());
                continue;
            }

            if (command.charAt(i) == '\"') {
                i++;
                while (i < command.length() && command.charAt(i) != '\"') {
                    string.append(command.charAt(i));
                    i++;
                }
                arguments.add(string.toString());
                string.delete(0, string.length());
                continue;
            }

            if (command.charAt(i) == ' ') {
                if (!string.isEmpty()) {
                    arguments.add(string.toString());
                    string.delete(0, string.length());
                }
            } else
                string.append(command.charAt(i));
        }
        arguments.add(string.toString());
        String name = arguments.get(0);
        arguments.remove(0);

        HashMap<String, String> option = new HashMap<>();
        for (String argument : arguments) {
            String[] kv = argument.split(":");
            if (kv.length == 2) {
                // Put option name and value
                option.put(kv[0], kv[1]);
            }
        }

        return new ConsoleCommandEvent(name, option);
    }

    public String getCommandName() {
        return name;
    }

    public boolean hasOption(String option) {
        return optionMap.containsKey(option);
    }

    public String getValue(String option) {
        return optionMap.get(option);
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(String option, Class<T> clazz) {
        if (optionMap.containsKey(option))
            try {
                return (T) optionMap.get(option);
            } catch (Exception e) {
                throw new IllegalArgumentException();
            }

        return null;
    }
}
