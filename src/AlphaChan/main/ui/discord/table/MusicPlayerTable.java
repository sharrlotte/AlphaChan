package AlphaChan.main.ui.discord.table;

import AlphaChan.BotConfig;
import AlphaChan.main.music.MusicPlayer;
import AlphaChan.main.ui.discord.Table;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;

public class MusicPlayerTable extends Table {

    private final MusicPlayer player;

    public MusicPlayerTable(SlashCommandInteractionEvent event, MusicPlayer player) {
        super(event, 10);
        this.player = player;

        addButton(button("play", ButtonStyle.PRIMARY, Emoji.fromUnicode(player.getTrackStatus()), () -> player.play()));
        addButton(button("next", ButtonStyle.PRIMARY, Emoji.fromUnicode(BotConfig.TEmoji.FORWARD.value),
                () -> player.playNext()));
        addButton(button("clear", ButtonStyle.PRIMARY, Emoji.fromUnicode(BotConfig.TEmoji.CLEAR.value),
                () -> player.clear()));
        addButton(button("X", ButtonStyle.DANGER, () -> deleteTable()));

        player.setTable(this);
        onTimeOut.connect((n) -> player.setTable(null));
        onPrepareTable.connect(this::onPrepareTable);

    }

    public void onPrepareTable(MessageEditAction action) {
        setButton(button("play", ButtonStyle.PRIMARY, Emoji.fromUnicode(player.getTrackStatus()), () -> player.play()));
    }
}
