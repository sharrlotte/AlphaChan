package AlphaChan.main.gui.discord.table;

import AlphaChan.BotConfig;
import AlphaChan.main.command.SimplePageTable;
import AlphaChan.main.music.MusicPlayer;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageUpdateAction;

public class MusicPlayerTable extends SimplePageTable {

    private final MusicPlayer player;

    public MusicPlayerTable(SlashCommandInteractionEvent event, MusicPlayer player) {
        super(event, 10);
        this.player = player;

        addButton(primary("play", Emoji.fromMarkdown(player.getTrackStatus()), () -> player.play()));
        addButton(primary("next", Emoji.fromMarkdown(BotConfig.FORWARD_EMOJI), () -> player.playNext()));
        addButton(primary("clear", Emoji.fromMarkdown(BotConfig.CLEAR_EMOJI), () -> player.clear()));
        addButton(deny("X", () -> this.deleteTable()));

        player.setTable(this);
        onTimeOut.connect((n) -> player.setTable(null));
    }

    @Override
    public void updateTable() {

        resetTimer();
        setButton(primary("play", Emoji.fromMarkdown(player.getTrackStatus()), () -> {
            player.play();
            updateTable();
        }));

        WebhookMessageUpdateAction<Message> action = event.getHook()
                .editOriginalEmbeds(player.getEmbedBuilder().build()).setActionRows(getButton());

        action.queue();
    }
}