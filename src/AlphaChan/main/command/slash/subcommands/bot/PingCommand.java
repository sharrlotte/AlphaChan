package AlphaChan.main.command.slash.subcommands.bot;

import AlphaChan.main.command.SlashSubcommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class PingCommand extends SlashSubcommand {

    public PingCommand() {
        super("ping", "Hiển thị tốc độ kết nối giữa bot và discord");
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        long ping = event.getJDA().getGatewayPing();

        reply(event, "Ping: " + ping + "ms", 10);
    }

}
