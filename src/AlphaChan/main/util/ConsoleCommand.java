package AlphaChan.main.util;

import java.util.ArrayList;
import java.util.List;

public class ConsoleCommand {

    private List<String> arguments;
    private int index = 0;

    public ConsoleCommand(String command) {
        arguments = new ArrayList<>();
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
    }

    public List<String> getArguments() {
        return arguments;
    }

    public int getArgumentCount() {
        return arguments.size() - 1;
    }

    public String getName() {
        return arguments.get(0);
    }

    public String nextString() {
        index += 1;
        return arguments.get(index);
    }

    public int nextInt() {
        index += 1;
        return Integer.parseInt(arguments.get(index));
    }

    public float nextFloat() {
        index += 1;
        return Float.parseFloat(arguments.get(index));
    }
}
