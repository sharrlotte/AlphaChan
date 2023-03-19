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

import AlphaChan.main.gui.discord.table.MusicPlayerTable;
import AlphaChan.main.util.Log;
import AlphaChan.main.util.StringUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.awt.Color;

public class MusicPlayer extends AudioEventAdapter implements AudioSendHandler {

    public final static float MAX_TRACK_LENGTH = 60f; // Max 60 minutes video

    public final static String PLAY_EMOJI = "\u25B6"; // ▶
    public final static String PAUSE_EMOJI = "\u23F8"; // ⏸
    public final static String STOP_EMOJI = "\u23F9"; // ⏹

    private AudioPlayer audioPlayer;
    private AudioFrame lastFrame;
    private Guild guild;
    private AudioChannel channel;

    private MusicPlayerTable table;

    private LinkedList<QueuedTrack> queue = new LinkedList<QueuedTrack>();

    public MusicPlayer(Guild guild, AudioPlayer player) {

        this.audioPlayer = player;
        this.guild = guild;
    }

    public void start(AudioChannel channel) {
        guild.getAudioManager().openAudioConnection(channel);
    }

    public void play() {
        audioPlayer.setPaused(!audioPlayer.isPaused());
    }

    public void play(QueuedTrack track) {
        audioPlayer.playTrack(track.getTrack());
    }

    public boolean playNext() {
        boolean result = false;

        if (!queue.isEmpty()) {
            play(queue.removeFirst());
            result = true;
        }

        if (table == null)
            return result;

        table.updateTable();
        return result;
    }

    public void destroy() {
        guild.getAudioManager().closeAudioConnection();
    }

    public boolean addTrack(QueuedTrack queueTrack) {
        boolean result = false;
        if (audioPlayer.getPlayingTrack() == null) {
            play(queueTrack);

        } else {
            queue.add(queueTrack);
            result = true;
        }

        if (table != null)
            table.updateTable();

        return result;
    }

    public boolean addTracks(List<QueuedTrack> queueTrack) {
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

        if (table != null)
            table.updateTable();

        return result;
    }

    public void clear() {
        queue.clear();
        audioPlayer.stopTrack();
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

                    String url = "http://img.youtube.com/vi/" + matcher.group(1) + "/0.jpg";
                    builder.setThumbnail(url);
                }

                builder.addField("Now playing",
                        "Tác giả: " + playing.getInfo().author + //
                                "\nVideo: [" + playing.getInfo().title + "](" + playing.getInfo().uri + ")" + //
                                "\nThời lượng: " + StringUtils.toTime(playing.getDuration()) + //
                                "\nNgười yêu cầu: " + playing.getUserData(RequestMetadata.class).getRequester(),

                        false);
            }
        } catch (Exception e) {
            Log.error(e);
        }

        StringBuffer songList = new StringBuffer();
        Iterator<QueuedTrack> it = queue.iterator();
        QueuedTrack current;
        int count = 1;

        while (it.hasNext()) {
            current = it.next();
            // Max field string length is 1024
            if (songList.length() + current.getTrack().getInfo().title.length() > 900)
                break;

            songList.append("\t" + count + ":    " + current.getTrack().getInfo().title + "\n");
            count++;
        }

        builder.addField("Play list", songList.toString(), false);

        return builder;
    }

    public void setTable(MusicPlayerTable table) {
        if (this.table != null) {
            this.table.delete();
        }
        this.table = table;

    }

    public String getTrackStatus() {
        return audioPlayer.isPaused() ? PAUSE_EMOJI : PLAY_EMOJI;

    }

    public Guild getGuild() {
        return guild;
    }

    public AudioChannel getChannel() {
        return channel;
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        if (table == null)
            return;

        table.updateTable();
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

}
