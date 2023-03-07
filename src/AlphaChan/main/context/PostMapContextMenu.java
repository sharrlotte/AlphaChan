package AlphaChan.main.context;

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import AlphaChan.main.handler.MessageHandler;
import AlphaChan.main.util.SimpleBotContextMenu;

public class PostMapContextMenu extends SimpleBotContextMenu {

    public PostMapContextMenu() {
        super("Post Map");
    }

    @Override
    protected void runCommand(MessageContextInteractionEvent event) {
        MessageHandler.sendMapPreview(event.getTarget(), event.getTextChannel());
        event.getHook().deleteOriginal().queue();
    }
}
