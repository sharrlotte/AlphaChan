package alpha.main.ui.discord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import alpha.main.data.user.TimeObject;
import alpha.main.event.Signal;
import alpha.main.handler.MessageHandler;
import alpha.main.handler.TableHandler;
import alpha.main.util.Log;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import net.dv8tion.jda.internal.interactions.component.ButtonImpl;

public abstract class Table extends TimeObject {

    private static final String SEPARATOR = ":";

    private final SlashCommandInteractionEvent event;

    private List<CallbackButton> buttons = new ArrayList<CallbackButton>();
    private List<Integer> rows = new ArrayList<Integer>(Arrays.asList(0));

    private ButtonInteractionEvent interaction;

    private String requestor;
    private Message message;

    public Signal<MessageEditAction> onPrepareTable = new Signal<>();

    public Table(SlashCommandInteractionEvent event, int aliveLimit) {
        super(aliveLimit);
        this.event = event;

        onTimeOut.connect((n) -> deleteTable());
        onUpdate.connect((n) -> updateTable());

        event.getInteraction().getHook().deleteOriginal().queue();

        TableHandler.addTable(this);
    }

    public SlashCommandInteractionEvent getEvent() {
        return event;
    }

    public ButtonInteractionEvent getInteraction() {
        return interaction;
    }

    public Message getMessage() {
        return message;
    }

    public void setRequester(String userId) {
        this.requestor = userId;
    }

    public void setRequester(Member member) {
        if (member != null)
            setRequester(member.getId());
    }

    public void onCommand(ButtonInteractionEvent event) {
        this.interaction = event;

        String key = event.getComponentId();

        resetTimer();

        Log.info("INTERACTION", event.getMember().getEffectiveName() + " pressed button " + key);

        if (requestor != null) {
            if (!getTriggerMember().getId().equals(requestor)) {
                MessageHandler.replyTranslate(event.getHook(),
                        "<error.no_table_interact_permission>[Error: You don't have permission to interact with this table]",
                        10);
                return;
            }
        }

        for (CallbackButton b : buttons) {
            String id = b.getId();
            if (id.equals(key)) {
                b.getRunnable().run();
                break;
            }
        }
    }

    public final Table sendTable() {
        message = event.getChannel().sendMessage(EmbedBuilder.ZERO_WIDTH_SPACE).complete();
        updateTable();
        return this;
    }

    public final void updateTable() {

        if (message == null)
            message = event.getChannel().sendMessage(EmbedBuilder.ZERO_WIDTH_SPACE).complete();

        MessageEditAction action = message.editMessage(EmbedBuilder.ZERO_WIDTH_SPACE);

        onPrepareTable.emit(action);

        resetTimer();
        setButtons(action, false);

        action.queue();
    }

    public synchronized void deleteTable() {

        if (isAlive()) {

            if (message != null) {
                message.delete().complete();
                message = null;
            }

            kill();
        }
    }

    public Guild getEventGuild() {
        Guild guild = event.getGuild();
        if (guild == null)
            throw new IllegalStateException("GUILD IS NOT EXISTS");
        return guild;
    }

    public Member getEventMember() {
        Member member = event.getMember();
        if (member == null)
            throw new IllegalStateException("MEMBER IS NOT EXISTS");
        return member;
    }

    public GuildMessageChannel getEventTextChannel() {
        return event.getGuildChannel();
    }

    public String getId() {
        return event.getId();
    }

    public Guild getTriggerGuild() {
        Guild guild = interaction.getGuild();
        if (guild == null)
            throw new IllegalStateException("GUILD IS NOT EXISTS");
        return guild;
    }

    public Member getTriggerMember() {
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

    public MessageChannelUnion getTriggerTextChannel() {
        return interaction.getChannel();
    }

    public String getButtonName() {
        if (interaction == null)
            return null;
        return interaction.getComponentId();
    }

    public CallbackButton button(String buttonName, ButtonStyle style, Runnable runnable) {
        Button button = new ButtonImpl(getId() + SEPARATOR + buttonName, buttonName, style, false, null);
        CallbackButton callbackButton = new CallbackButton(button, runnable);

        return callbackButton;
    }

    public CallbackButton button(String buttonId, String buttonName, ButtonStyle style, Runnable runnable) {
        Button button = new ButtonImpl(getId() + SEPARATOR + buttonId, buttonName, style, false, null);
        CallbackButton callbackButton = new CallbackButton(button, runnable);

        return callbackButton;
    }

    public CallbackButton button(String buttonId, ButtonStyle style, Emoji emoji, Runnable runnable) {
        Button button = new ButtonImpl(getId() + SEPARATOR + buttonId, "", style, false, emoji);
        CallbackButton callbackButton = new CallbackButton(button, runnable);

        return callbackButton;
    }

    public void addButton(CallbackButton callbackButton) {
        buttons.add(callbackButton);
        int row = rows.size() - 1;
        int number = rows.get(row);
        rows.set(row, number + 1);
    }

    public void setButton(CallbackButton replace) {
        for (int i = 0; i < buttons.size(); i++) {
            if (buttons.get(i).getId().equals(replace.getId())) {
                buttons.set(i, replace);
                break;
            }
        }
    }

    public void addRow() {
        rows.add(0);
    }

    public MessageEditAction setButtons(MessageEditAction action, boolean disable) {
        Collection<LayoutComponent> rows = getButtons();
        if (rows.size() > 0) {
            rows.forEach((b) -> b = b.withDisabled(disable));

            action.setComponents(rows);
        }

        return action;
    }

    public Collection<LayoutComponent> getButtons() {
        Collection<LayoutComponent> row = new ArrayList<>();
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
        rows = new ArrayList<Integer>(Arrays.asList(0));
        buttons.clear();
    }

    private class CallbackButton {
        private final Runnable runnable;
        private final Button button;

        public CallbackButton(Button button, Runnable runnable) {
            this.runnable = runnable;
            this.button = button;
        }

        public Runnable getRunnable() {
            return runnable;
        }

        public Button getButton() {
            return button;
        }

        public String getId() {
            return getButton().getId();
        }
    }
}
