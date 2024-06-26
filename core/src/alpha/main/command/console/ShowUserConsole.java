package alpha.main.command.console;

import alpha.main.command.ConsoleCommandEvent;
import alpha.main.command.ConsoleCommandOptionData.OptionType;
import alpha.main.command.ConsoleAutoCompleteEvent;
import alpha.main.command.ConsoleCommand;
import alpha.main.data.user.UserCache;
import alpha.main.handler.UserHandler;
import alpha.main.ui.bot.AutoCompleteTextField;
import alpha.main.util.Log;
import alpha.main.util.StringUtils;

public class ShowUserConsole extends ConsoleCommand {

    public ShowUserConsole() {
        super("show-user", "Show user status");
        addCommandOption("guildid", OptionType.STRING, "Guild id");
        addCommandOption("userid", OptionType.STRING, "User id");
    }

    @Override
    public void onCommand(ConsoleCommandEvent event) {

        String guildId = event.getOption("guildid");
        String userId = event.getOption("userid");

        if (guildId == null || guildId.isEmpty() || userId == null || userId.isEmpty()) {
            Log.info("USER STATUS", "Users count: " + UserHandler.getActiveUserCount());
            for (UserCache ud : UserHandler.getUserCache()) {
                Log.info("USER STATUS",
                        "\n" + StringUtils.mapToLines(ud.getData().toDocument()) + "Time = " + ud.getTime() + "\n");
            }

        } else {
            try {
                if (guildId == null || userId == null)
                    throw new IllegalArgumentException();

                UserCache ud = UserHandler.getUserNoCache(guildId, userId);
                Log.info("USER STATUS", "User id: " + userId + //
                        "\n" + StringUtils.mapToLines(ud.getData().toDocument()) + "\n");
            } catch (Exception e) {
                Log.warning("User with id " + userId + " in guild " + guildId + " not found");
            }
        }
    }

    @Override
    public void onAutoComplete(ConsoleAutoCompleteEvent event) {
        if (event.getFocusString().equals("userid")) {
            event.replyChoices(event.getFocusValue(), StringUtils//
                    .findBestMatches(event.getFocusValue(),
                            UserHandler.getUserCache()//
                                    .stream().map((user) -> user.getData().getUserId()).toList(),
                            10)
                    .stream().map((id) -> new AutoCompleteTextField.Choice(id, id)).toList());
        } else if (event.getFocusString().equals("guildid")) {
            event.replyChoices(event.getFocusValue(), StringUtils//
                    .findBestMatches(event.getFocusValue(),
                            UserHandler.getUserCache()//
                                    .stream().map((user) -> user.getData().getGuildId()).toList(),
                            10)
                    .stream().map((id) -> new AutoCompleteTextField.Choice(id, id)).toList());
        }
    }
}
