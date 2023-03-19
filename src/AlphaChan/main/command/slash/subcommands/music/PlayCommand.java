package AlphaChan.main.command.slash.subcommands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import AlphaChan.main.command.SimpleBotSubcommand;
import AlphaChan.main.gui.discord.table.MusicPlayerTable;
import AlphaChan.main.handler.MessageHandler;
import AlphaChan.main.handler.MusicPlayerHandler;
import AlphaChan.main.music.MusicPlayer;
import AlphaChan.main.music.QueuedTrack;
import AlphaChan.main.util.Log;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class PlayCommand extends SimpleBotSubcommand {

    public PlayCommand() {
        super("play", "Phát một bản nhạc");
        addOption(OptionType.STRING, "source", "Link/Tên bài hát muốn phát");
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {

        OptionMapping sourceOption = event.getOption("source");

        try {
            if (sourceOption != null) {

                String source = sourceOption.getAsString();

                if (source.isBlank()) {
                    reply(event, "Không thể tải liên kết, liên kết không hợp lệ", 10);
                    return;
                }

                Member member = event.getMember();
                GuildVoiceState voiceState = member.getVoiceState();
                AudioChannel channel = voiceState.getChannel();

                if (!voiceState.inAudioChannel() && channel == null) {
                    reply(event, "Bạn phải ở trong kênh thoại để sử dụng lệnh này", 10);
                    return;
                }

                MusicPlayerHandler.getInstance().loadItemOrdered(event.getGuild(), source, new ResultHandler(event));

                if (!MusicPlayerHandler.getInstance().hasMusicPlayer(event.getGuild())) {
                    new MusicPlayerTable(event, MusicPlayerHandler.getInstance().getMusicPlayer(event.getGuild()))
                            .sendTable();
                } else {
                    event.getHook().deleteOriginal().queue();
                    MusicPlayerHandler.getInstance().getMusicPlayer(event.getGuild()).start(channel);
                }
            } else {

                new MusicPlayerTable(event, MusicPlayerHandler.getInstance().getMusicPlayer(event.getGuild()))
                        .sendTable();
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
            if (!MusicPlayer.isValidTrack(track)) {
                reply(event, "Đoạn video này quá dài để có thể phát", 30);
                return;
            }

            MusicPlayer handler = (MusicPlayer) event.getGuild().getAudioManager().getSendingHandler();

            handler.addTrack(new QueuedTrack(track, event.getMember()));
            MessageHandler.sendMessage(event.getChannel(), "Tải thành công: " + track.getInfo().title, 10);
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            StringBuffer string = new StringBuffer();
            for (AudioTrack track : playlist.getTracks()) {

                if (!MusicPlayer.isValidTrack(track)) {

                    MusicPlayer handler = (MusicPlayer) event.getGuild().getAudioManager().getSendingHandler();
                    handler.addTrack(new QueuedTrack(track, event.getMember()));
                    string.append(track.getInfo().title + "\n");
                }
            }
            MessageHandler.sendMessage(event.getChannel(), string.toString(), 10);
        }

        @Override
        public void noMatches() {
            MessageHandler.sendMessage(event.getChannel(), "Không có kết quả tìm kiếm", 10);
        }

        @Override
        public void loadFailed(FriendlyException throwable) {
            MessageHandler.sendMessage(event.getChannel(), "Lỗi khi tải " + throwable.getMessage(), 10);
        }
    }
}
