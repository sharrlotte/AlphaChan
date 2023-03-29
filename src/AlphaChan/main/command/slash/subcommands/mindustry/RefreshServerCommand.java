package AlphaChan.main.command.slash.subcommands.mindustry;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import AlphaChan.main.command.SlashSubcommand;
import AlphaChan.main.handler.MessageHandler;
import AlphaChan.main.handler.ServerStatusHandler;

public class RefreshServerCommand extends SlashSubcommand {
    public RefreshServerCommand() {
        super("refreshserver", "<?command.command_refresh_server>");
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        ServerStatusHandler.refreshServerStat(event.getGuild(), event.getMessageChannel());
        MessageHandler.reply(event, "<?command.mindustry_server_refreshed>", 10);
    }

}
