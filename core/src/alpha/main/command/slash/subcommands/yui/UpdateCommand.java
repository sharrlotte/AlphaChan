package alpha.main.command.slash.subcommands.yui;

import alpha.main.command.SlashSubcommand;
import alpha.main.handler.CommandHandler;
import alpha.main.handler.MessageHandler;
import alpha.main.util.StringUtils;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class UpdateCommand extends SlashSubcommand {
    public UpdateCommand() {
        super("updatecommand", "Yui only", false, true);
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        CommandHandler.updateCommand();
        MessageHandler.reply(event.getHook(), StringUtils.backtick("Slash command updated"), 10);
    }
}
