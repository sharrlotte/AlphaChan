package AlphaChan.main.command.subcommands.YuiCommands;

import AlphaChan.main.handler.UpdatableHandler;
import AlphaChan.main.util.SimpleBotSubcommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class UpdateCommand extends SimpleBotSubcommand {
    public UpdateCommand() {
        super("updatecommand", "Yui only", false, true);
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        UpdatableHandler.updateCommand();
    }
}
