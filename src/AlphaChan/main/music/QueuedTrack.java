package AlphaChan.main.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import AlphaChan.main.util.StringUtils;
import net.dv8tion.jda.api.entities.Member;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class QueuedTrack {
    private final AudioTrack track;

    public QueuedTrack(AudioTrack track, Member owner) {
        this(track, new RequestMetadata(owner));
    }

    public QueuedTrack(AudioTrack track, RequestMetadata rm) {
        this.track = track;
        this.track.setUserData(rm);
    }

    public long getIdentifier() {
        return track.getUserData(RequestMetadata.class).getOwner();
    }

    public AudioTrack getTrack() {
        return track;
    }

    @Override
    public String toString() {
        String entry = "`[" + StringUtils.toTime(track.getDuration()) + "]` ";
        AudioTrackInfo trackInfo = track.getInfo();
        entry = entry + (trackInfo.uri.startsWith("http") ? "[**" + trackInfo.title + "**](" + trackInfo.uri + ")"
                : "**" + trackInfo.title + "**");
        return entry + " - <@" + track.getUserData(RequestMetadata.class).getOwner() + ">";
    }
}
