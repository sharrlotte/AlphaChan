package AlphaChan.main.command.slash.subcommands.music;

import AlphaChan.main.command.SlashSubcommand;
import AlphaChan.main.handler.MessageHandler;
import AlphaChan.main.handler.MusicPlayerHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class SkipCommand extends SlashSubcommand {

    public SkipCommand() {
        super("skip", "<?command.command_music_skip>");
        addOption(OptionType.INTEGER, "number", "<?command.number>", true);
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        OptionMapping numberOption = event.getOption("number");

        if (numberOption == null)
            return;

        MusicPlayerHandler.getInstance().getMusicPlayer(event.getGuild()).skip(numberOption.getAsInt());
        MessageHandler.delete(event);
    }
}
