package AlphaChan.main.command.slash.subcommands.music;

import AlphaChan.main.command.SimpleBotSubcommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class PlayCommand extends SimpleBotSubcommand {

    public PlayCommand() {
        super("play", "Phát một bản nhạc");
        addOption(OptionType.STRING, "url", "Link bản nhạc muốn phát");
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent command) {

    }

}
