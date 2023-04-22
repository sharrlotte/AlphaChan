package alpha.main.command.console;

import alpha.main.command.ConsoleCommandEvent;
import alpha.main.BotConfig;
import alpha.main.command.ConsoleCommand;
import alpha.main.util.Log;

public class SaveConfigConsole extends ConsoleCommand {

    public SaveConfigConsole() {
        super("save-config", "Save bot config");
    }

    @Override
    public void onCommand(ConsoleCommandEvent event) {
        BotConfig.save();
        Log.system("Config saved");
    }

}
