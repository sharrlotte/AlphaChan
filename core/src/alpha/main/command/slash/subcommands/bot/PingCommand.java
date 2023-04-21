package alpha.main.command.slash.subcommands.bot;

import alpha.main.command.SlashSubcommand;
import alpha.main.handler.MessageHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class PingCommand extends SlashSubcommand {

    public PingCommand() {
        super("ping", "<command.command_bot_ping>[Get bot ping]");
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        long ping = event.getJDA().getGatewayPing();

        MessageHandler.reply(event.getHook(), "Ping: " + ping + "ms", 10);
    }

}
