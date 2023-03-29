package AlphaChan.main.command.slash.subcommands.mindustry;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import AlphaChan.main.command.SlashSubcommand;
import AlphaChan.main.handler.MessageHandler;
import AlphaChan.main.handler.NetworkHandler;
import AlphaChan.main.handler.ServerStatusHandler;
import mindustry.net.Host;

public class PingCommand extends SlashSubcommand {
    public PingCommand() {
        super("ping", "<?command.command_ping_mindustry_server>");
        this.addOption(OptionType.STRING, "ip", "<?command.mindustry_server_ip>", true);
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        OptionMapping ipOption = event.getOption("ip");
        if (ipOption == null)
            return;
        String ip = ipOption.getAsString();
        NetworkHandler.pingServer(ip, result -> extracted(event, ip, result));
    }

    private void extracted(SlashCommandInteractionEvent event, String ip, Host result) {
        EmbedBuilder builder = ServerStatusHandler.serverStatusBuilder(ip, result);
        MessageHandler.replyEmbed(event, builder, 30);
    }

}
