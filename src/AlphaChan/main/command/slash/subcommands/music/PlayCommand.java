package AlphaChan.main.command.slash.subcommands.music;

import java.util.ArrayList;
import java.util.List;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import AlphaChan.main.command.SlashSubcommand;
import AlphaChan.main.gui.discord.table.MusicPlayerTable;
import AlphaChan.main.handler.MessageHandler;
import AlphaChan.main.handler.MusicPlayerHandler;
import AlphaChan.main.music.MusicPlayer;
import AlphaChan.main.music.MusicTrack;
import AlphaChan.main.util.Log;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class PlayCommand extends SlashSubcommand {

    public PlayCommand() {
        super("play", "Play a song");
        addOption(OptionType.STRING, "source", "Song link");
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {

        OptionMapping sourceOption = event.getOption("source");

        try {
            if (sourceOption != null) {

                String source = sourceOption.getAsString();

                if (source.isBlank()) {
                    MessageHandler.reply(event, "<?command.invalid_link", 10);
                    return;
                }

                Member member = event.getMember();
                GuildVoiceState voiceState = member.getVoiceState();
                AudioChannelUnion channel = voiceState.getChannel();

                if (!voiceState.inAudioChannel() && channel == null) {
                    MessageHandler.reply(event, "Bạn phải ở trong kênh thoại để sử dụng lệnh này", 10);
                    return;
                }

                MusicPlayerHandler.getInstance().loadItemOrdered(event.getGuild(), source, new ResultHandler(event));

                if (!MusicPlayerHandler.getInstance().hasMusicPlayer(event.getGuild())) {
                    new MusicPlayerTable(event, MusicPlayerHandler.getInstance().getMusicPlayer(event.getGuild())).sendTable();
                } else {

                    event.getHook().deleteOriginal().queue();
                }

                MusicPlayerHandler.getInstance().getMusicPlayer(event.getGuild()).start(channel);
            } else {

                new MusicPlayerTable(event, MusicPlayerHandler.getInstance().getMusicPlayer(event.getGuild())).sendTable();
            }
        } catch (Exception e) {
            Log.error(e);
        }
    }

    private class ResultHandler implements AudioLoadResultHandler {
        private final SlashCommandInteractionEvent event;

        private ResultHandler(SlashCommandInteractionEvent event) {
            this.event = event;
        }

        @Override
        public void trackLoaded(AudioTrack track) {

            MusicPlayer handler = (MusicPlayer) event.getGuild().getAudioManager().getSendingHandler();

            handler.addTrack(new MusicTrack(track, event.getMember()));
            MessageHandler.sendMessage(event.getGuildChannel(), "Tải thành công: " + track.getInfo().title, 10);
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            StringBuffer string = new StringBuffer();
            int count = 0;

            List<MusicTrack> tracks = new ArrayList<>();

            MusicPlayer handler = (MusicPlayer) event.getGuild().getAudioManager().getSendingHandler();
            playlist.getTracks().forEach((track) -> tracks.add(new MusicTrack(track, event.getMember())));
            handler.addTracks(tracks);

            for (AudioTrack track : playlist.getTracks()) {

                if (string.length() + track.getInfo().title.length() < Message.MAX_CONTENT_LENGTH)
                    string.append(track.getInfo().title + "\n");
                else
                    count += 1;

            }
            MessageHandler.sendMessage(event.getGuildChannel(),
                    "Tải thành công: " + string.toString() + (count == 0 ? "" : "và " + count + " bài hát khác"), 10);
        }

        @Override
        public void noMatches() {
            MessageHandler.sendMessage(event.getGuildChannel(), "Không có kết quả tìm kiếm", 10);
        }

        @Override
        public void loadFailed(FriendlyException throwable) {
            MessageHandler.sendMessage(event.getGuildChannel(), "Lỗi khi tải " + throwable.getMessage(), 10);
        }
    }
}
