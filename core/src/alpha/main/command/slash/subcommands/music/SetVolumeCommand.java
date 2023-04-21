package alpha.main.command.slash.subcommands.music;

import alpha.main.command.SlashSubcommand;
import alpha.main.handler.MessageHandler;
import alpha.main.handler.MusicPlayerHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class SetVolumeCommand extends SlashSubcommand {

    public SetVolumeCommand() {
        super("setvolume", "<command.command_set_volume>[Set the bot volume]");
        addOption(OptionType.INTEGER, "volume", "<command.volume>[Volume]", true);
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        OptionMapping volumeOption = event.getOption("volume");

        if (volumeOption == null)
            return;

        MusicPlayerHandler.getInstance().getMusicPlayer(event.getGuild()).setVolume(volumeOption.getAsInt());
        MessageHandler.delete(event.getHook());
    }
}
