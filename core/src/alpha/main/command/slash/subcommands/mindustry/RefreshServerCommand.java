package alpha.main.command.slash.subcommands.mindustry;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import alpha.main.command.SlashSubcommand;
import alpha.main.handler.MessageHandler;
import alpha.main.handler.ServerStatusHandler;

public class RefreshServerCommand extends SlashSubcommand {
    public RefreshServerCommand() {
        super("refreshserver", "<command.command_refresh_server>[Refresh mindustry server list]");
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        ServerStatusHandler.refreshServerStat(event.getGuild(), event.getMessageChannel());
        MessageHandler.replyTranslate(event.getHook(),
                "<command.mindustry_server_refreshed>[Mindustry server list refreshed]", 10);
    }

}
