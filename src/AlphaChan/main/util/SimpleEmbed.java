package AlphaChan.main.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import AlphaChan.main.handler.TableHandler;
import AlphaChan.main.user.DataCache;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class SimpleEmbed extends DataCache {

    private List<RunnableButton> buttons = new ArrayList<RunnableButton>();
    private List<Integer> rows = new ArrayList<Integer>(Arrays.asList(0));

    private final String SEPARATOR = ":";

    protected ButtonInteractionEvent interaction;
    protected final SlashCommandInteractionEvent event;

    public SimpleEmbed(SlashCommandInteractionEvent event, int aliveLimit) {
        super(aliveLimit, 0);
        this.event = event;
        TableHandler.add(this);
    }

    public void onCommand(@Nonnull ButtonInteractionEvent event) {
        this.interaction = event;

        String key = event.getComponentId();

        for (RunnableButton b : buttons) {
            String id = b.getId();
            if (id.equals(key)) {
                b.getRunnable().run();
                break;
            }
        }
    }

    public void finalize() {
        this.delete();
    }

    public void delete() {
        this.event.getHook().deleteOriginal().queue();
        this.killTimer();
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

    public String getId() {
        return this.event.getId();
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

    public Message getTriggerMessage() {
        if (interaction == null)
            return null;
        return interaction.getMessage();
    }

    public @Nonnull TextChannel getTriggerTextChannel() {
        return interaction.getTextChannel();
    }

    public String getButtonName() {
        if (this.interaction == null)
            return null;
        return interaction.getComponentId();
    }

    public SimpleEmbed addButtonPrimary(@Nonnull String buttonName, @Nonnull Runnable runable) {
        Button button = Button.primary(getId() + SEPARATOR + buttonName, buttonName);
        RunnableButton tableButton = new RunnableButton(button, runable);

        addButton(tableButton);
        return this;
    }

    public SimpleEmbed addButtonPrimary(@Nonnull String buttonId, @Nonnull String buttonName,
            @Nonnull Runnable runable) {
        Button button = Button.primary(getId() + SEPARATOR + buttonId, buttonName);
        RunnableButton tableButton = new RunnableButton(button, runable);

        addButton(tableButton);
        return this;
    }

    public SimpleEmbed addButtonPrimary(@Nonnull String buttonId,
            @Nonnull Emoji emo,
            @Nonnull Runnable runable) {
        Button button = Button.primary(getId() + SEPARATOR + buttonId, emo);
        RunnableButton tableButton = new RunnableButton(button, runable);

        addButton(tableButton);
        return this;
    }

    public SimpleEmbed addButtonSuccess(@Nonnull String buttonName, @Nonnull Emoji emo, @Nonnull Runnable runable) {
        Button button = Button.success(getId() + SEPARATOR + buttonName, emo);
        RunnableButton tableButton = new RunnableButton(button, runable);

        addButton(tableButton);
        return this;
    }

    public SimpleEmbed addButtonSuccess(@Nonnull String buttonId, @Nonnull String buttonName,
            @Nonnull Runnable runable) {
        Button button = Button.success(getId() + SEPARATOR + buttonId, buttonName);
        RunnableButton tableButton = new RunnableButton(button, runable);

        addButton(tableButton);
        return this;
    }

    public SimpleEmbed addButtonDeny(@Nonnull String buttonName, @Nonnull Runnable runable) {
        Button button = Button.danger(getId() + SEPARATOR + buttonName, buttonName);
        RunnableButton tableButton = new RunnableButton(button, runable);

        addButton(tableButton);
        return this;
    }

    public SimpleEmbed addButtonDeny(@Nonnull String buttonName, @Nonnull Emoji emo, @Nonnull Runnable runable) {
        Button button = Button.danger(getId() + SEPARATOR + buttonName, emo);
        RunnableButton tableButton = new RunnableButton(button, runable);

        addButton(tableButton);
        return this;
    }

    public SimpleEmbed addButtonDeny(@Nonnull String buttonId, @Nonnull String buttonName, @Nonnull Runnable runable) {
        Button button = Button.danger(getId() + SEPARATOR + buttonId, buttonName);
        RunnableButton tableButton = new RunnableButton(button, runable);

        addButton(tableButton);
        return this;
    }

    public void addButton(RunnableButton tableButton) {
        buttons.add(tableButton);
        int row = rows.size() - 1;
        int number = rows.get(row);
        rows.set(row, number + 1);
    }

    public void addRow() {
        rows.add(0);
    }

    public @Nonnull Collection<ActionRow> getButton() {
        Collection<ActionRow> row = new ArrayList<ActionRow>();
        if (buttons.size() == 0)
            return row;
        List<Button> button = new ArrayList<>();
        int m = 0;
        for (int n : rows) {
            for (int i = 0; i < n; i++) {
                button.add(buttons.get(m).getButton());
                m++;
            }
            if (button.size() > 0) {
                row.add(ActionRow.of(button));
                button.clear();
            }
        }
        return row;
    }

    public void clearButton() {
        this.rows = new ArrayList<Integer>(Arrays.asList(0));
        this.buttons.clear();
    }

    public void reply(String content) {
        this.event.getHook().editOriginal("```" + content + "```").queue();
    }

    public void reply(String content, int deleteAfter) {
        this.event.getHook().editOriginal("```" + content + "```").queue();
    }

    public void sendMessage(String content, int deleteAfter) {
        if (this.interaction == null)
            return;
        this.interaction.getHook().sendMessage("```" + content + "```")
                .queue(m -> m.delete().queueAfter(deleteAfter, TimeUnit.SECONDS));
    }

    public void sendMessage(String content, boolean ephemeral) {
        if (this.interaction == null)
            return;
        this.interaction.getHook().sendMessage("```" + content + "```").setEphemeral(ephemeral).queue();
    }

    private class RunnableButton {
        private final Runnable runable;
        private final Button button;

        public RunnableButton(@Nonnull Button button, @Nonnull Runnable runable) {
            this.runable = runable;
            this.button = button;
        }

        public Runnable getRunnable() {
            return this.runable;
        }

        public Button getButton() {
            return this.button;
        }

        public String getId() {
            return this.getButton().getId();
        }
    }

}
