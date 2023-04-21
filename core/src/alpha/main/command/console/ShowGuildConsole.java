package alpha.main.command.console;

import alpha.main.command.ConsoleCommandEvent;
import alpha.main.command.ConsoleCommandOptionData.OptionType;
import alpha.main.command.ConsoleCommand;
import alpha.main.data.user.GuildCache;
import alpha.main.handler.GuildHandler;
import alpha.main.util.Log;
import alpha.main.util.StringUtils;

public class ShowGuildConsole extends ConsoleCommand {

    public ShowGuildConsole() {
        super("show-guild", "Show guild");
        addOption("guildid", OptionType.STRING, "Guild id");
    }

    @Override
    public void runCommand(ConsoleCommandEvent event) {

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
}
