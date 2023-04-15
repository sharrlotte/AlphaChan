package AlphaChan.main.ui.discord;

import java.util.ArrayList;
import java.util.List;

import AlphaChan.main.handler.MessageHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;

public class PageTable extends Table {

    private List<EmbedBuilder> table = new ArrayList<EmbedBuilder>();
    protected int pageNumber = 0;
    protected boolean showPageNumber = true;

    public PageTable(SlashCommandInteractionEvent event, int aliveLimit) {
        super(event, aliveLimit);

        onPrepareTable.connect(this::onPrepareTable);
    }

    public boolean addPage(EmbedBuilder value) {
        if (value == null || value.isEmpty())
            return false;
        return table.add(new EmbedBuilder(value));
    }

    public EmbedBuilder getCurrentPage() {
        EmbedBuilder value = table.get(pageNumber);
        return addPageFooter(value);
    }

    public EmbedBuilder addPageFooter(EmbedBuilder value) {
        if (showPageNumber)
            return value.setFooter("Trang " + (pageNumber + 1) + "\\" + getMaxPage());
        return value;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
        updateTable();
    }

    public int getMaxPage() {
        return table.size();
    }

    public void nextPage() {
        pageNumber += 1;
        pageNumber %= getMaxPage();
        updateTable();
    }

    public void previousPage() {
        pageNumber -= 1;
        if (pageNumber <= -1)
            pageNumber = getMaxPage() - 1;
        updateTable();
    }

    public void firstPage() {
        pageNumber = 0;
        updateTable();
    }

    public void lastPage() {
        pageNumber = getMaxPage() - 1;
        updateTable();
    }

    public void onPrepareTable(MessageEditAction action) {
        MessageEmbed page = getCurrentPage().build();
        if (getMaxPage() <= 0 || page == null) {
            MessageHandler.sendMessage(getEventTextChannel(), "Không có dữ liệu", 10);
            return;
        }

        action.setEmbeds(page);
    }
}
