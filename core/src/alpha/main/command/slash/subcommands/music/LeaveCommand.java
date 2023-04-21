package alpha.main.command.slash.subcommands.music;

import alpha.main.command.SlashSubcommand;
import alpha.main.handler.MessageHandler;
import alpha.main.music.MusicPlayer;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class LeaveCommand extends SlashSubcommand {

    public LeaveCommand() {
        super("leave", "<command.command_music_bot_leave>[Make the bot leave voice channel]");
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        MusicPlayer player = (MusicPlayer) event.getGuild().getAudioManager().getSendingHandler();
        player.leave();
        MessageHandler.delete(event.getHook());
    }
}
