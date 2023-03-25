package AlphaChan.main.command.slash.subcommands.admin;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import AlphaChan.main.command.SlashSubcommand;
import AlphaChan.main.handler.ServerStatusHandler;

public class ReloadServerCommand extends SlashSubcommand {
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
