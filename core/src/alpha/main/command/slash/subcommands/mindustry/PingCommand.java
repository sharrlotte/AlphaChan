package alpha.main.command.slash.subcommands.mindustry;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import alpha.main.command.SlashSubcommand;
import alpha.main.handler.MessageHandler;
import alpha.main.handler.NetworkHandler;
import alpha.main.handler.ServerStatusHandler;
import mindustry.net.Host;

public class PingCommand extends SlashSubcommand {
    public PingCommand() {
        super("ping", "<command.command_ping_mindustry_server>[Ping a mindustry server]");
        this.addCommandOption(OptionType.STRING, "ip", "<command.mindustry_server_ip>[Mindustry server ip]", true);
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        OptionMapping ipOption = event.getOption("ip");
        if (ipOption == null)
            return;
        String ip = ipOption.getAsString();
        NetworkHandler.pingServer(ip, result -> extracted(event, ip, result));
    }

    private void extracted(SlashCommandInteractionEvent event, String ip, Host result) {
        EmbedBuilder builder = ServerStatusHandler.serverStatusBuilder(ip, result);
        MessageHandler.replyEmbed(event.getHook(), builder, 30);
    }

}
