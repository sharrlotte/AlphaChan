package AlphaChan.main.music;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import AlphaChan.BotConfig;
import AlphaChan.main.ui.discord.table.MusicPlayerTable;
import AlphaChan.main.util.Log;
import AlphaChan.main.util.StringUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.audio.hooks.ConnectionListener;
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.awt.Color;

public class MusicPlayer extends AudioEventAdapter implements AudioSendHandler {

    private AudioPlayer audioPlayer;
    private AudioFrame lastFrame;
    private Guild guild;
    private AudioChannel channel;

    private MusicPlayerTable table;

    private LinkedList<MusicTrack> queue = new LinkedList<MusicTrack>();

    public MusicPlayer(Guild guild, AudioPlayer player) {

        this.audioPlayer = player;
        this.guild = guild;

        // guild.getAudioManager().setConnectionListener(new MusicListener());
    }

    public void start(AudioChannel channel) {
        this.channel = channel;
        guild.getAudioManager().openAudioConnection(channel);
    }

    public void play() {

        if (audioPlayer.getPlayingTrack() == null)
            playNext();
        else
            audioPlayer.setPaused(!audioPlayer.isPaused());

        guild.getAudioManager().openAudioConnection(channel);
        updateTable();
    }

    public void play(MusicTrack track) {
        audioPlayer.playTrack(track.getTrack());
    }

    public void playNext() {
        audioPlayer.stopTrack();

        if (!queue.isEmpty()) {
            play(queue.removeFirst());
            return;
        }

        if (table == null)
            return;

        if (channel != null)
            start(channel);

        table.updateTable();
    }

    public void skip(int number) {
        if (number <= 0)
            return;

        if (number >= queue.size()) {
            clear();
            return;
        }

        for (int i = 0; i < number - 1; i++)
            queue.removeFirst();

        playNext();
    }

    public void leave() {
        if (guild == null)
            return;

        clear();
        guild.getAudioManager().closeAudioConnection();
    }

    public void clear() {
        queue.clear();
        audioPlayer.stopTrack();
        updateTable();
    }

    public void addVolume(int amount) {

        int volume = audioPlayer.getVolume();
        volume += amount;
        setVolume(volume);
    }

    public void setVolume(int volume) {
        volume = Math.max(0, Math.min(100, volume));
        audioPlayer.setVolume(volume);
        updateTable();
    }

    public boolean addTrack(MusicTrack queueTrack) {
        boolean result = false;
        if (audioPlayer.getPlayingTrack() == null) {
            play(queueTrack);

        } else {
            queue.add(queueTrack);
            result = true;
        }

        updateTable();
        return result;
    }

    public boolean addTracks(List<MusicTrack> queueTrack) {
        boolean result = false;

        if (queueTrack.isEmpty())
            return result;

        if (audioPlayer.getPlayingTrack() == null) {
            play(queueTrack.get(0));
            queueTrack.remove(0);
            queue.addAll(queueTrack);

        } else {
            queue.addAll(queueTrack);
            result = true;
        }

        updateTable();
        return result;
    }

    private void updateTable() {
        if (table != null)
            table.updateTable();
    }

    public EmbedBuilder getEmbedBuilder() {
        EmbedBuilder builder = new EmbedBuilder();
        Member self = guild.getSelfMember();

        builder.setAuthor(self.getEffectiveName(), null, self.getEffectiveAvatarUrl());
        builder.setColor(Color.BLUE);

        try {
            AudioTrack playing = audioPlayer.getPlayingTrack();

            if (playing != null) {
                Pattern pattern = Pattern.compile("v=(.+)");
                Matcher matcher = pattern.matcher(playing.getInfo().uri);

                if (matcher.find()) {

                    String url = "http://img.youtube.com/vi/" + matcher.group(1) + "/maxresdefault.jpg";
                    builder.setImage(url);
                }

                builder.addField("Now playing", "Tác giả: " + playing.getInfo().author + //
                        "\nVideo: [" + playing.getInfo().title + "](" + playing.getInfo().uri + ")" + //
                        "\nThời lượng: " + TimeFormat.RELATIVE.now().minus(playing.getPosition()) + "/"
                        + StringUtils.toTime(playing.getDuration()) + //

                        "\nNgười yêu cầu: " + playing.getUserData(RequestMetadata.class).getRequester() + "\nÂm lượng: "
                        + StringUtils.getProgressBar(audioPlayer.getVolume() / 100d, 20, "||", "|", "|")
                        + audioPlayer.getVolume() + "%",

                        false);
            }
        } catch (Exception e) {
            Log.error(e);
        }

        StringBuffer songList = new StringBuffer();
        Iterator<MusicTrack> it = queue.iterator();
        MusicTrack current;
        int count = 0;
        boolean overload = false;

        while (it.hasNext()) {
            current = it.next();
            count++;
            if (songList.length() + current.getTrack().getInfo().title.length() < MessageEmbed.TEXT_MAX_LENGTH) {
                songList.append("\t" + count + ": " + current.getTrack().getInfo().title + "\n");
            } else
                overload = true;
        }

        builder.addField("Play list", songList.toString().replace("||", "") + (overload ? "..." + count : ""), false);

        return builder;
    }

    public void setTable(MusicPlayerTable table) {
        if (this.table != null) {
            this.table.deleteTable();
        }
        this.table = table;

    }

    public String getTrackStatus() {
        return audioPlayer.isPaused() ? BotConfig.TEmoji.PAUSE.value : BotConfig.TEmoji.PLAY.value;

    }

    public Guild getGuild() {
        return guild;
    }

    public AudioChannel getChannel() {
        return channel;
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        updateTable();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason == AudioTrackEndReason.FINISHED)
            playNext();
    }

    @Override
    public boolean canProvide() {
        lastFrame = audioPlayer.provide();
        return lastFrame != null;
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        return ByteBuffer.wrap(lastFrame.getData());
    }

    @Override
    public boolean isOpus() {
        return true;
    }

    public class MusicListener implements ConnectionListener {

        @Override
        public void onPing(long ping) {
            Log.system(String.valueOf(ping));
        }

        @Override
        public void onStatusChange(ConnectionStatus status) {
            Log.error(status.toString() + " Auto reconnect: " + guild.getAudioManager().isAutoReconnect() + " Muted: "
                    + guild.getAudioManager().isSelfMuted());
            if (guild.getAudioManager().isConnected()) {
                Log.system("Connected");
            } else {
                Log.system("Disconnected");

            }
        }

        @Override
        public void onUserSpeaking(User user, boolean speaking) {

        }

    }

}
