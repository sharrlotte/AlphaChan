package alpha.main.command;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConsoleCommandEvent {

    private final String name;
    private final HashMap<String, String> optionMap;

    public static final Pattern COMMAND_NAME_PATTERN = Pattern.compile("/([\\w\\-]+)");
    public static final Pattern COMMAND_OPTION_PATTERN = Pattern
            .compile("(\\w+)=(([\\w\\-_]+)|\\{([\\w\s,\\-_]+)\\}|\\\"([\\w\s,\\-_]+)\\\")");

    private ConsoleCommandEvent(String name, HashMap<String, String> optionMap) {
        this.name = name;
        this.optionMap = optionMap;
    }

    protected ConsoleCommandEvent(ConsoleCommandEvent event) {
        this.name = event.name;
        this.optionMap = event.optionMap;
    }

    public static ConsoleCommandEvent parseCommand(String command) {

        Matcher nameMatcher = COMMAND_NAME_PATTERN.matcher(command);
        String name = nameMatcher.find() ? nameMatcher.group(1) : null;

        if (name == null)
            new ConsoleCommandEvent(null, null);

        Matcher optionMatcher = COMMAND_OPTION_PATTERN.matcher(command);
        HashMap<String, String> options = new HashMap<>();
        while (optionMatcher.find()) {
            options.put(optionMatcher.group(1), optionMatcher.group(2));
        }

        return new ConsoleCommandEvent(name, options);

    }

    public String getCommandName() {
        return name;
    }

    public boolean hasOption(String option) {
        return optionMap.containsKey(option);
    }

    public String getOption(String option) {
        String value = optionMap.get(option);
        return value;
    }

    public String getOptionAsString(String option) {
        String value = optionMap.get(option);
        value = value.startsWith("\"") ? value.substring(1, value.length()) : value;
        value = value.endsWith("\"") ? value.substring(0, value.length() - 1) : value;
        return value;
    }

    @SuppressWarnings("unchecked")
    public <T> T getOptionAs(String option, Class<T> clazz) {
        if (optionMap.containsKey(option))
            try {
                return (T) optionMap.get(option);

            } catch (Exception e) {
                throw new IllegalArgumentException();
            }

        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> void getOptionAsList(String option, Class<T> clazz, List<T> result) {
        if (optionMap.containsKey(option)) {
            try {
                String value = optionMap.get(option);
                value = value.startsWith("{") ? value.substring(1, value.length()) : value;
                value = value.endsWith("}") ? value.substring(0, value.length() - 1) : value;
                for (String v : value.split(",")) {
                    result.add((T) v);
                }

            } catch (Exception e) {
                throw new IllegalArgumentException();
            }
        }
    }
}
