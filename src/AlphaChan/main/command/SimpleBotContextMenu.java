package AlphaChan.main.command;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public abstract class SimpleBotContextMenu {

    private String name;
    @Nonnull
    public CommandData command;

    public SimpleBotContextMenu(@Nonnull String name) {
        this.name = name;
        command = Commands.message(name);
    }

    public String getName() {
        return this.name;
    }

    public void onCommand(MessageContextInteractionEvent event) {
        runCommand(event);
    }

    public abstract void runCommand(MessageContextInteractionEvent event);

    protected void replyEmbed(MessageContextInteractionEvent event, EmbedBuilder builder, int deleteAfter) {
        event.getHook().sendMessageEmbeds(builder.build())
                .queue(_message -> _message.delete().queueAfter(deleteAfter, TimeUnit.SECONDS));
    }

    protected void reply(MessageContextInteractionEvent event, String content, int deleteAfter) {
        event.getHook().sendMessage("```" + content + "```")
                .queue(_message -> _message.delete().queueAfter(deleteAfter, TimeUnit.SECONDS));
    }

}
