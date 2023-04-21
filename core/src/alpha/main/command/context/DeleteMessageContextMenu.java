package alpha.main.command.context;

import alpha.main.command.ContextMenuCommand;
import alpha.main.handler.MessageHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.AuthorInfo;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;

public class DeleteMessageContextMenu extends ContextMenuCommand {

    public DeleteMessageContextMenu() {
        super("Delete Message");
    }

    @Override
    public void runCommand(MessageContextInteractionEvent event) {
        Member member = event.getTarget().getMember();

        if (member == null)
            return;

        if (member.getUser().isBot()) {
            for (MessageEmbed embed : event.getTarget().getEmbeds()) {
                AuthorInfo authorInfo = embed.getAuthor();
                if (authorInfo == null)
                    continue;
                String name = authorInfo.getName();
                if (name == null)
                    continue;
                Member trigger = event.getMember();
                if (trigger == null)
                    continue;

                if (name.equals(trigger.getEffectiveName())) {
                    event.getTarget().delete().queue();
                    MessageHandler.replyTranslate(event.getHook(),
                            "<context.delete_message_successfully>[Delete successfully]", 10);
                    return;
                }
                MessageHandler.replyTranslate(event.getHook(),
                        "<context.delete_message_failed>[Can't delete other's request]", 10);

            }
        } else {
            MessageHandler.replyTranslate(event.getHook(),
                    "<context.delete_user_message_failed>[Can't delete user's message, this command only use to delete bot message]",
                    10);
        }
    }
}
