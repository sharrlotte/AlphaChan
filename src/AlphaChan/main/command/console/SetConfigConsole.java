package AlphaChan.main.command.console;

import AlphaChan.BotConfig;
import AlphaChan.main.command.ConsoleCommandEvent;
import AlphaChan.main.command.ConsoleCommand;
import AlphaChan.main.util.Log;
import AlphaChan.main.util.StringUtils;

public class SetConfigConsole extends ConsoleCommand {

    public SetConfigConsole() {
        super("set-config", "\n\t- <key> <value>: Set bot configure");
    }

    @Override
    public void runCommand(ConsoleCommandEvent command) {

        // String key = command.nextString();
        // String value = command.nextString();

        // if (BotConfig.hasProperty(key)) {
        //     Log.system("Set " + key + " from [" + BotConfig.readString(key, value) + "] to [" + value + "]");
        //     BotConfig.setProperty(key, value);
        // } else {
        //     String est = StringUtils.findBestMatch(key, BotConfig.getProperties().keySet());
        //     if (est == null)
        //         Log.error("Key " + key + " doesn't exists");
        //     else
        //         Log.error("Key " + key + " doesn't exists, do you mean " + est);
        // }
    }
}
