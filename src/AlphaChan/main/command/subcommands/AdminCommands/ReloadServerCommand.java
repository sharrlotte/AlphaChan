package AlphaChan.main.command.subcommands.AdminCommands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import AlphaChan.main.handler.ServerStatusHandler;
import AlphaChan.main.util.SimpleBotSubcommand;

public class ReloadServerCommand extends SimpleBotSubcommand {
    public ReloadServerCommand() {
        super("reloadserver", "Tải lại tất cả máy chủ mindustry");
    }

    @Override
    public String getHelpString() {
        return "Tải lại tất cả máy chủ mindustry";
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        ServerStatusHandler.reloadServer(event.getGuild(), event.getMessageChannel());
        reply(event, "Đã làm mới máy chủ", 10);
    }
}
