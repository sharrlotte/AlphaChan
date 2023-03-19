package AlphaChan.main.gui.discord.table;

import AlphaChan.main.command.SimplePageTable;
import AlphaChan.main.handler.MessageHandler;
import AlphaChan.main.music.MusicPlayer;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageUpdateAction;

public class MusicPlayerTable extends SimplePageTable {

    private final MusicPlayer player;

    public MusicPlayerTable(SlashCommandInteractionEvent event, MusicPlayer player) {
        super(event, 30);
        this.player = player;

        addButton(primary("play", Emoji.fromMarkdown(player.getTrackStatus()), () -> {
            player.play();
            updateTable();
        }));

        addButton(primary("next", Emoji.fromMarkdown("⏭️"), () -> {
            if (player.playNext())
                MessageHandler.sendMessage(getEventTextChannel(), "Danh sách phát trống", 10);
        }));

        addButton(deny("X", () -> this.delete()));

        player.setTable(this);
    }

    @Override
    public void updateTable() {

        setButton(primary("play", Emoji.fromMarkdown(player.getTrackStatus()), () -> player.play()));

        WebhookMessageUpdateAction<Message> action = event.getHook()
                .editOriginalEmbeds(player.getEmbedBuilder().build()).setActionRows(getButton());

        action.queue();
    }

}
