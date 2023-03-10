package AlphaChan.main.music;

import java.nio.ByteBuffer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import arc.struct.Queue;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;

public class MusicPlayer extends AudioEventAdapter implements AudioSendHandler {

    public final static String PLAY_EMOJI = "\u25B6"; // ▶
    public final static String PAUSE_EMOJI = "\u23F8"; // ⏸
    public final static String STOP_EMOJI = "\u23F9"; // ⏹

    private AudioPlayer audioPlayer;
    private AudioFrame lastFrame;
    private Guild guild;
    private VoiceChannel channel;

    private Queue<QueuedTrack> queue = new Queue<QueuedTrack>();

    public MusicPlayer(Guild guild, AudioPlayer player) {

        this.audioPlayer = player;
        this.guild = guild;
    }

    public int addTrackToFront(QueuedTrack queueTrack) {
        if (audioPlayer.getPlayingTrack() == null) {
            audioPlayer.playTrack(queueTrack.getTrack());
            return -1;
        } else {
            queue.addFirst(queueTrack);
            return 0;
        }
    }

    public int addTrack(QueuedTrack queueTrack) {
        if (audioPlayer.getPlayingTrack() == null) {
            audioPlayer.playTrack(queueTrack.getTrack());
            return -1;
        } else
            queue.addLast(queueTrack);
        return 1;
    }

    public void clear() {
        queue.clear();
        audioPlayer.stopTrack();
    }

    public Queue<QueuedTrack> getQueue() {
        return queue;
    }

    public Guild getGuild() {
        return guild;
    }

    public VoiceChannel getChannel() {
        return channel;
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
