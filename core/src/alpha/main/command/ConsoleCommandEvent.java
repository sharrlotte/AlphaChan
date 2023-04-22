package alpha.main.command;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConsoleCommandEvent {

    private final String name;
    private final HashMap<String, String> optionMap;

    private static final Pattern COMMAND_NAME_PATTERN = Pattern.compile("([\\w\\-]+)");
    private static final Pattern COMMAND_OPTION_PATTERN = Pattern.compile("(\\w+)=\\[([\\w\s{},\\-]+)\\]");

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
            return null;

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
        return optionMap.get(option);
    }

    @SuppressWarnings("unchecked")
    public <T> T getOption(String option, Class<T> clazz) {
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
                for (String value : optionMap.get(option).split(",")) {
                    result.add((T) value);
                }

            } catch (Exception e) {
                throw new IllegalArgumentException();
            }
        }
    }
}
