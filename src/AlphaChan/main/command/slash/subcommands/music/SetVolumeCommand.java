package AlphaChan.main.command.slash.subcommands.music;

import AlphaChan.main.command.SlashSubcommand;
import AlphaChan.main.handler.MessageHandler;
import AlphaChan.main.handler.MusicPlayerHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class SetVolumeCommand extends SlashSubcommand {

    public SetVolumeCommand() {
        super("setvolume", "<@command.command_set_volume>");
        addOption(OptionType.INTEGER, "volume", "<@command.volume>", true);
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        OptionMapping volumeOption = event.getOption("volume");

        if (volumeOption == null)
            return;

        MusicPlayerHandler.getInstance().getMusicPlayer(event.getGuild()).setVolume(volumeOption.getAsInt());
        MessageHandler.delete(event);
    }
}
