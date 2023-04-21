package alpha.main.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.Member;

public class MusicTrack {
    private final AudioTrack track;

    public MusicTrack(AudioTrack track, Member owner) {
        this(track, new RequestMetadata(owner));
    }

    public MusicTrack(AudioTrack track, RequestMetadata rm) {
        this.track = track;
        this.track.setUserData(rm);
    }

    public long getIdentifier() {
        return track.getUserData(RequestMetadata.class).getOwner();
    }

    public String getRequester() {
        return track.getUserData(RequestMetadata.class).getRequester();
    }

    public AudioTrack getTrack() {
        return track;
    }
}
