package AlphaChan.main.command.console;

import AlphaChan.BotConfig;
import AlphaChan.main.command.ConsoleCommandEvent;
import AlphaChan.main.command.ConsoleCommand;
import AlphaChan.main.util.Log;

public class SaveConfigConsole extends ConsoleCommand {

    public SaveConfigConsole() {
        super("save-config", "Save bot config");
    }

    @Override
    public void runCommand(ConsoleCommandEvent command) {
        BotConfig.save();
        Log.system("Config saved");
    }

}
