package AlphaChan.main.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageUpdateAction;

public class SimplePageTable extends SimpleTable {

    private List<EmbedBuilder> table = new ArrayList<EmbedBuilder>();
    protected int pageNumber = 0;
    protected boolean showPageNumber = true;

    public SimplePageTable(SlashCommandInteractionEvent event, int aliveLimit) {
        super(event, aliveLimit);

        onTimeOut.connect((n) -> delete());
    }

    public boolean addPage(EmbedBuilder value) {
        if (value == null || value.isEmpty())
            return false;
        return table.add(new EmbedBuilder(value));
    }

    public MessageEmbed getCurrentPage() {
        EmbedBuilder value = table.get(pageNumber);
        return addPageFooter(value).build();
    }

    public EmbedBuilder addPageFooter(EmbedBuilder value) {
        if (showPageNumber)
            return value.setFooter("Trang " + (pageNumber + 1) + "\\" + getMaxPage());
        return value;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
        update();
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

    public SimplePageTable sendTable() {
        updateTable();
        return this;
    }

    public void updateTable() {
        resetTimer();
        if (getMaxPage() <= 0) {
            event.getHook().editOriginal("```Không có dữ liệu```").queue();
            return;
        }
        MessageEmbed message = getCurrentPage();
        if (message == null) {
            event.getHook().editOriginal("```Đã hết dữ liệu```").queue();
            return;
        }
        WebhookMessageUpdateAction<Message> action = event.getHook().editOriginalEmbeds(message);

        Collection<ActionRow> row = getButton();
        if (row.size() > 0)
            action.setActionRows(row);
        action.queue();
    }
}
