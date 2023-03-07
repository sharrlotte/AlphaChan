package AlphaChan.main.command.subcommands.AdminCommands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import AlphaChan.main.handler.CommandHandler;
import AlphaChan.main.handler.ContextMenuHandler;
import AlphaChan.main.util.SimpleBotSubcommand;

public class RefreshSlashCommand extends SimpleBotSubcommand {
    public RefreshSlashCommand() {
        super("refreshslashcommand", "Làm mới lại tất cả các lệnh trong máy chủ");
    }

    @Override
    public String getHelpString() {
        return "Làm mới tất cả các lệnh trong máy chủ";
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        CommandHandler.unregisterCommand(event.getGuild());
        ContextMenuHandler.unregisterCommand(event.getGuild());
        CommandHandler.registerCommand(event.getGuild());
        ContextMenuHandler.registerCommand(event.getGuild());
        reply(event, "Đã làm mới lệnh", 30);
    }
}
