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

    public static HashMap<String, MusicPlayer> musicPlayers = new HashMap<>();

    public MusicPlayerHandler() {

        AudioSourceManagers.registerRemoteSources(this);
        source(YoutubeAudioSourceManager.class).setPlaylistPageCount(10);
    }

    public boolean hasMusicPlayer(Guild guild) {
        return guild.getAudioManager().getSendingHandler() != null && musicPlayers.containsKey(guild.getId());
    }

    public MusicPlayer getMusicPlayer(Guild guild) {

        MusicPlayer musicPlayer;

        if (hasMusicPlayer(guild)) {
            return musicPlayers.get(guild.getId());

        } else {
            AudioPlayer player = createPlayer();
            // TODO Add volume setting
            player.setVolume(100);
            musicPlayer = new MusicPlayer(guild, player);
            player.addListener(musicPlayer);
            guild.getAudioManager().setSendingHandler(musicPlayer);
            musicPlayers.put(guild.getId(), musicPlayer);

            return musicPlayer;
        }
    }
}
