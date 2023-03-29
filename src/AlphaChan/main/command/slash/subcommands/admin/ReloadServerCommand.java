package AlphaChan.main.command.slash.subcommands.admin;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import AlphaChan.main.command.SlashSubcommand;
import AlphaChan.main.handler.MessageHandler;
import AlphaChan.main.handler.ServerStatusHandler;

public class ReloadServerCommand extends SlashSubcommand {
    public ReloadServerCommand() {
        super("reloadserver", "<@command.command_reload_mindustry_server>");
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        ServerStatusHandler.reloadServer(event.getGuild(), event.getMessageChannel());
        MessageHandler.reply(event, "<@command.server_refreshed>", 10);
    }
}
