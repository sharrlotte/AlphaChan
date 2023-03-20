package AlphaChan.main.command.slash.subcommands.music;

import AlphaChan.main.command.SimpleBotSubcommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class LeaveCommand extends SimpleBotSubcommand {

    public LeaveCommand() {
        super("leave", "Khiến bot rời khỏi phòng nhạc");
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        event.getGuild().getAudioManager().closeAudioConnection();
    }
}
