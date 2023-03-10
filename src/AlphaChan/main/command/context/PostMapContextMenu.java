package AlphaChan.main.command.context;

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import AlphaChan.main.command.SimpleBotContextMenu;
import AlphaChan.main.handler.MessageHandler;

public class PostMapContextMenu extends SimpleBotContextMenu {

    public PostMapContextMenu() {
        super("Post Map");
    }

    @Override
    public void runCommand(MessageContextInteractionEvent event) {
        MessageHandler.sendMapPreview(event.getTarget(), event.getTextChannel());
        event.getHook().deleteOriginal().queue();
    }
}
