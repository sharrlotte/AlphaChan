package AlphaChan.main.console;

import AlphaChan.main.handler.UserHandler;
import AlphaChan.main.user.UserData;
import AlphaChan.main.util.ConsoleCommand;
import AlphaChan.main.util.Log;
import AlphaChan.main.util.SimpleConsoleCommand;

public class ShowUserConsole extends SimpleConsoleCommand {

    public ShowUserConsole() {
        super("show-user", "\n\t- <>: Show all guild that has been cached" + //
                "\n\t- <guild id> <user id>: Show specific user");
    }

    @Override
    public void runCommand(ConsoleCommand command) {

        if (command.getArgumentCount() == 0) {
            Log.info("USER STATUS", "Users count: " + UserHandler.getActiveUserCount());
            for (UserData ud : UserHandler.getUserCache()) {
                Log.info("USER STATUS", ud.toDocument().toJson().toString());
            }
        } else if (command.getArgumentCount() == 2) {
            String guildId = command.nextString();
            String userId = command.nextString();
            try {
                UserData ud = UserHandler.getUserNoCache(guildId, userId);
                Log.info("USER STATUS", "User id: " + userId + //
                        "\n" + ud.toDocument().toString());
            } catch (Exception e) {
                Log.warning("User with id " + userId + " in guild " + guildId + " not found");
            }
        } else {
            Log.error("Command require [0-1] arguments ");
        }
    }
}
