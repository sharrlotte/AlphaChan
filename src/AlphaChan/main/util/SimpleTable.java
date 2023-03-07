package AlphaChan.main.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageUpdateAction;

public class SimpleTable extends SimpleEmbed {

    private List<EmbedBuilder> table = new ArrayList<EmbedBuilder>();
    protected int pageNumber = 0;
    protected boolean showPageNumber = true;

    public SimpleTable(SlashCommandInteractionEvent event, int aliveLimit) {
        super(event, aliveLimit);
    }

    public void finalize() {
        this.delete();
    }

    public @Nonnull Guild getEventGuild() {
        Guild guild = event.getGuild();
        if (guild == null)
            throw new IllegalStateException("GUILD IS NOT EXISTS");
        return guild;
    }

    public @Nonnull Member getEventMember() {
        Member member = event.getMember();
        if (member == null)
            throw new IllegalStateException("MEMBER IS NOT EXISTS");
        return member;
    }

    public TextChannel getEventTextChannel() {
        return event.getTextChannel();
    }

    public @Nonnull Guild getTriggerGuild() {
        Guild guild = interaction.getGuild();
        if (guild == null)
            throw new IllegalStateException("GUILD IS NOT EXISTS");
        return guild;
    }

    public @Nonnull Member getTriggerMember() {
        Member member = interaction.getMember();
        if (member == null)
            throw new IllegalStateException("MEMBER IS NOT EXISTS");
        return member;
    }

    public @Nonnull TextChannel getTriggerTextChannel() {
        return interaction.getTextChannel();
    }

    public String getButtonName() {
        if (this.interaction == null)
            return null;
        return interaction.getComponentId();
    }

    public boolean addPage(EmbedBuilder value) {
        if (value == null || value.isEmpty())
            return false;
        return table.add(new EmbedBuilder(value));
    }

    public MessageEmbed getCurrentPage() {
        EmbedBuilder value = this.table.get(pageNumber);
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
        return this.table.size();
    }

    public void nextPage() {
        this.pageNumber += 1;
        this.pageNumber %= getMaxPage();
        this.updateTable();
    }

    public void previousPage() {
        this.pageNumber -= 1;
        if (this.pageNumber <= -1)
            this.pageNumber = getMaxPage() - 1;
        this.updateTable();
    }

    public void firstPage() {
        this.pageNumber = 0;
        this.updateTable();
    }

    public void lastPage() {
        this.pageNumber = getMaxPage() - 1;
        this.updateTable();
    }

    public void sendTable() {
        updateTable();
    }

    public void updateTable() {
        this.resetTimer();
        if (getMaxPage() <= 0) {
            event.getHook().editOriginal("```Không có dữ liệu```").queue();
            return;
        }
        MessageEmbed message = getCurrentPage();
        if (message == null) {
            event.getHook().editOriginal("```Đã hết dữ liệu```").queue();
            return;
        }
        Collection<ActionRow> row = getButton();
        WebhookMessageUpdateAction<Message> action = this.event.getHook().editOriginalEmbeds(message);
        if (row.size() > 0)
            action.setActionRows(row);
        action.queue();
    }
}
