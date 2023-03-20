package AlphaChan.main.command.slash.subcommands.yui;

import AlphaChan.main.command.SimpleBotSubcommand;
import AlphaChan.main.handler.CommandHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class UpdateCommand extends SimpleBotSubcommand {
    public UpdateCommand() {
        super("updatecommand", "Yui only", false, true);
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        CommandHandler.updateCommand();
    }
}