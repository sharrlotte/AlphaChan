package AlphaChan.main.handler;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;

import AlphaChan.main.music.MusicPlayer;
import net.dv8tion.jda.api.entities.Guild;

public class MusicPlayerHandler extends DefaultAudioPlayerManager {

    // https://github.com/jagrosh/MusicBot/tree/master/src/main/java/com/jagrosh/jmusicbot/audio

    public MusicPlayerHandler() {

        AudioSourceManagers.registerRemoteSources(this);
        AudioSourceManagers.registerLocalSource(this);
        source(YoutubeAudioSourceManager.class).setPlaylistPageCount(20);
    }

    public boolean hasMusicPlayer(Guild guild) {
        return guild.getAudioManager().getSendingHandler() != null;
    }

    public MusicPlayer setupMusicPlayer(Guild guild) {

        MusicPlayer musicPlayer;

        if (hasMusicPlayer(guild)) {
            return (MusicPlayer) guild.getAudioManager().getSendingHandler();
        } else {
            AudioPlayer player = createPlayer();
            // TODO Add volume setting
            player.setVolume(100);
            musicPlayer = new MusicPlayer(guild, player);
            player.addListener(musicPlayer);
            guild.getAudioManager().setSendingHandler(musicPlayer);
            return musicPlayer;
        }
    }
}
