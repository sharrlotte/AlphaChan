package AlphaChan.main.command.slash.subcommands.bot;

import AlphaChan.main.command.SlashSubcommand;
import AlphaChan.main.handler.MessageHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class PingCommand extends SlashSubcommand {

    public PingCommand() {
        super("ping", "<@command.command_bot_ping>");
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        long ping = event.getJDA().getGatewayPing();

        MessageHandler.reply(event, "Ping: " + ping + "ms", 10);
    }

}
