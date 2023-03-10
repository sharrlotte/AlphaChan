package AlphaChan.main.command.slash.subcommands.mindustry;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import AlphaChan.main.command.SimpleBotSubcommand;
import AlphaChan.main.handler.ServerStatusHandler;

public class RefreshServerCommand extends SimpleBotSubcommand {
    public RefreshServerCommand() {
        super("refreshserver", "Làm mới các máy chủ mindustry");
    }

    @Override
    public String getHelpString() {
        return "Làm mới các máy chủ mindustry";
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        ServerStatusHandler.refreshServerStat(event.getGuild(), event.getMessageChannel());
        reply(event, "Đã làm mới máy chủ", 10);
    }

}
