package alpha.main.command.console;

import alpha.main.command.ConsoleCommandEvent;
import alpha.main.command.ConsoleCommandOptionData.OptionType;
import alpha.main.command.ConsoleAutoCompleteEvent;
import alpha.main.command.ConsoleCommand;
import alpha.main.data.user.GuildCache;
import alpha.main.handler.GuildHandler;
import alpha.main.ui.bot.AutoCompleteTextField;
import alpha.main.util.Log;
import alpha.main.util.StringUtils;

public class ShowGuildConsole extends ConsoleCommand {

    public ShowGuildConsole() {
        super("show-guild", "Show guild");
        addCommandOption("guildid", OptionType.STRING, "Guild id");
    }

    @Override
    public void onCommand(ConsoleCommandEvent event) {

        String guildId = event.getOption("guildid");

        if (guildId == null || guildId.isEmpty()) {
            Log.info("GUILD STATUS", "Guild counts: " +
                    GuildHandler.getActiveGuildCount());

            for (GuildCache gd : GuildHandler.getGuildCache()) {
                Log.info("GUILD STATUS", "\n" +
                        StringUtils.mapToLines(gd.getData().toDocument()) + "\n");
            }
        } else {
            try {
                GuildCache gd = GuildHandler.getGuild(guildId);
                Log.info("GUILD STATUS", "Guild id: " + guildId + //
                        "\n" + StringUtils.mapToLines(gd.getData().toDocument()) + "\n");

            } catch (Exception e) {
                Log.warning("Guild with id " + guildId + " not found");
            }
        }
    }

    @Override
    public void onAutoComplete(ConsoleAutoCompleteEvent event) {
        if (event.getFocusString().equals("guildid")) {
            event.replyChoices(event.getFocusValue(), StringUtils//
                    .findBestMatches(event.getFocusValue(),
                            GuildHandler.getGuildCache()//
                                    .stream().map((guild) -> guild.getData().getGuildId()).toList(),
                            10)
                    .stream().map((id) -> new AutoCompleteTextField.Choice(id, id)).toList());
        }
    }
}
