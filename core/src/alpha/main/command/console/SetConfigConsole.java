package alpha.main.command.console;

import alpha.main.command.ConsoleCommandEvent;
import alpha.main.command.ConsoleCommandOptionData.OptionType;
import alpha.main.BotConfig;
import alpha.main.command.ConsoleCommand;
import alpha.main.util.Log;
import alpha.main.util.StringUtils;

public class SetConfigConsole extends ConsoleCommand {

    public SetConfigConsole() {
        super("set-config", "Set bot configure");
        addCommandOption("key", OptionType.STRING, "Key");
        addCommandOption("value", OptionType.STRING, "Value");
    }

    @Override
    public void onCommand(ConsoleCommandEvent event) {

        String key = event.getOption("key");
        String value = event.getOption("value");

        if (key == null || key.isEmpty() || value == null || value.isEmpty())
            Log.error("Key or value is empty");

        if (BotConfig.hasProperty(key)) {
            Log.system("Set " + key + " from [" + BotConfig.readString(key, value) + "] to [" + value + "]");
            BotConfig.setProperty(key, value);

        } else {
            String est = StringUtils.findBestMatch(key,
                    BotConfig.getProperties().keySet());
            if (est == null)
                Log.error("Key " + key + " doesn't exists");
            else
                Log.error("Key " + key + " doesn't exists, do you mean " + est);
        }
    }
}
