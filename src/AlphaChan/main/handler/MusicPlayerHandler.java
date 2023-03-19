package AlphaChan.main.handler;

import java.util.HashMap;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;

import AlphaChan.main.music.MusicPlayer;
import net.dv8tion.jda.api.entities.Guild;

public class MusicPlayerHandler extends DefaultAudioPlayerManager {

    // https://github.com/jagrosh/MusicBot/tree/master/src/main/java/com/jagrosh/jmusicbot/audio

    private static HashMap<String, MusicPlayer> musicPlayers = new HashMap<>();
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
        return guild.getAudioManager().getSendingHandler() != null && musicPlayers.containsKey(guild.getId());
    }

    public MusicPlayer getMusicPlayer(Guild guild) {

        if (hasMusicPlayer(guild)) {
            return musicPlayers.get(guild.getId());

        } else {
            AudioPlayer player = createPlayer();
            // TODO Add volume setting
            player.setVolume(100);
            MusicPlayer musicPlayer = new MusicPlayer(guild, player);
            player.addListener(musicPlayer);
            guild.getAudioManager().setSendingHandler(musicPlayer);
            musicPlayers.put(guild.getId(), musicPlayer);

            return musicPlayer;
        }
    }
}
