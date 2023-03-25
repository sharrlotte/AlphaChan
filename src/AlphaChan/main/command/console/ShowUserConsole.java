package AlphaChan.main.command.console;

import AlphaChan.main.command.ConsoleCommandEvent;
import AlphaChan.main.command.ConsoleCommand;
import AlphaChan.main.data.user.UserCache;
import AlphaChan.main.handler.UserHandler;
import AlphaChan.main.util.Log;

public class ShowUserConsole extends ConsoleCommand {

    public ShowUserConsole() {
        super("show-user", "\n\t- <>: Show all guild that has been cached" + //
                "\n\t- <guild id> <user id>: Show specific user");
    }

    @Override
    public void runCommand(ConsoleCommandEvent command) {

        if (command.getArgumentCount() == 0) {
            Log.info("USER STATUS", "Users count: " + UserHandler.getActiveUserCount());
            for (UserCache ud : UserHandler.getUserCache()) {
                Log.info("USER STATUS", ud.getData().toDocument().toString());
            }
        } else if (command.getArgumentCount() == 2) {
            String guildId = command.nextString();
            String userId = command.nextString();
            try {

                if (guildId == null || userId == null)
                    throw new IllegalArgumentException();

                UserCache ud = UserHandler.getUserNoCache(guildId, userId);
                Log.info("USER STATUS", "User id: " + userId + //
                        "\n" + ud.getData().toDocument().toString());
            } catch (Exception e) {
                Log.warning("User with id " + userId + " in guild " + guildId + " not found");
            }
        } else {
            Log.error("Command require [0-1] arguments ");
        }
    }
}
