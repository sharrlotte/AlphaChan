package AlphaChan.main.context;

import AlphaChan.main.util.SimpleBotContextMenu;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.AuthorInfo;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;

public class DeleteMessageContextMenu extends SimpleBotContextMenu {

    public DeleteMessageContextMenu() {
        super("Delete Message");
    }

    @Override
    protected void runCommand(MessageContextInteractionEvent event) {
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
                    return;
                }
                reply(event, "Không thể xóa tin nhắn của bot mà người khác đã yêu cầu", 10);

            }
        } else {
            reply(event, "Lệnh này chỉ có thể xóa tin nhắn của bot", 10);
        }
    }
}
