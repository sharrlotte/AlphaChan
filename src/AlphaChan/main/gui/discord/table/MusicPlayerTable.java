package AlphaChan.main.gui.discord.table;

import AlphaChan.BotConfig;
import AlphaChan.main.gui.discord.Table;
import AlphaChan.main.music.MusicPlayer;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;

public class MusicPlayerTable extends Table {

    private final MusicPlayer player;

    public MusicPlayerTable(SlashCommandInteractionEvent event, MusicPlayer player) {
        super(event, 10);
        this.player = player;

        addButton(primary("play", Emoji.fromUnicode(player.getTrackStatus()), () -> player.play()));
        addButton(primary("next", Emoji.fromUnicode(BotConfig.FORWARD_EMOJI), () -> player.playNext()));
        addButton(primary("clear", Emoji.fromUnicode(BotConfig.CLEAR_EMOJI), () -> player.clear()));
        addButton(deny("X", () -> this.deleteTable()));

        player.setTable(this);
        onTimeOut.connect((n) -> player.setTable(null));
        onPrepareTable.connect(this::onPrepareTable);

    }

    public void onPrepareTable(MessageEditAction action) {
        setButton(primary("play", Emoji.fromUnicode(player.getTrackStatus()), () -> player.play()));
    }
}
