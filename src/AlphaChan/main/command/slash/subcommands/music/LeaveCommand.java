package AlphaChan.main.command.slash.subcommands.music;

import AlphaChan.main.command.SlashSubcommand;
import AlphaChan.main.music.MusicPlayer;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class LeaveCommand extends SlashSubcommand {

    public LeaveCommand() {
        super("leave", "Khiến bot rời khỏi phòng nhạc");
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        MusicPlayer player = (MusicPlayer) event.getGuild().getAudioManager().getSendingHandler();
        player.leave();
        delete(event);
    }
}
