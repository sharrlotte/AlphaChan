package alpha.main.command.slash.subcommands.music;

import alpha.main.command.SlashSubcommand;
import alpha.main.handler.MessageHandler;
import alpha.main.handler.MusicPlayerHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class SkipCommand extends SlashSubcommand {

    public SkipCommand() {
        super("skip", "<command.command_music_skip>[Skip some song]");
        addCommandOption(OptionType.INTEGER, "number", "<command.number>[Number of song to skip]", true);
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        OptionMapping numberOption = event.getOption("number");

        if (numberOption == null)
            return;

        MusicPlayerHandler.getInstance().getMusicPlayer(event.getGuild()).skip(numberOption.getAsInt());
        MessageHandler.delete(event.getHook());
    }
}
