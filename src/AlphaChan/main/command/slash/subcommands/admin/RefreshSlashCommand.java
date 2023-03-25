package AlphaChan.main.command.slash.subcommands.admin;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import AlphaChan.main.command.SlashSubcommand;
import AlphaChan.main.handler.CommandHandler;

public class RefreshSlashCommand extends SlashSubcommand {
    public RefreshSlashCommand() {
        super("refreshslashcommand", "Làm mới lại tất cả các lệnh trong máy chủ");
    }

    @Override
    public String getHelpString() {
        return "Làm mới tất cả các lệnh trong máy chủ";
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        CommandHandler.updateCommand();
        reply(event, "Đã làm mới lệnh", 10);
    }
}
