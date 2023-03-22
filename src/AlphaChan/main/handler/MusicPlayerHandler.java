package AlphaChan.main.handler;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;

import AlphaChan.main.music.MusicPlayer;
import AlphaChan.main.util.Log;
import net.dv8tion.jda.api.entities.Guild;

public class MusicPlayerHandler extends DefaultAudioPlayerManager {

    // https://github.com/jagrosh/MusicBot/tree/master/src/main/java/com/jagrosh/jmusicbot/audio

    private static MusicPlayerHandler instance = new MusicPlayerHandler();

    public MusicPlayerHandler() {

        AudioSourceManagers.registerRemoteSources(this);
        source(YoutubeAudioSourceManager.class).setPlaylistPageCount(10);
    }

    public static MusicPlayerHandler getInstance() {
        if (instance == null)
            instance = new MusicPlayerHandler();

        return instance;
    }

    public boolean hasMusicPlayer(Guild guild) {
        return guild.getAudioManager().getSendingHandler() != null;
    }

    public MusicPlayer getMusicPlayer(Guild guild) {
        try {
            if (hasMusicPlayer(guild)) {
                return (MusicPlayer) guild.getAudioManager().getSendingHandler();

            } else {

                AudioPlayer player = createPlayer();
                MusicPlayer musicPlayer = new MusicPlayer(guild, player);
                player.addListener(musicPlayer);
                guild.getAudioManager().setSendingHandler(musicPlayer);

                return musicPlayer;
            }

        } catch (Exception e) {
            Log.error(e);
            return null;
        }

    }
}
