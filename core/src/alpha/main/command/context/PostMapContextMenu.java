package alpha.main.command.context;

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import alpha.main.command.ContextMenuCommand;
import alpha.main.handler.MessageHandler;

public class PostMapContextMenu extends ContextMenuCommand {

    public PostMapContextMenu() {
        super("Post Map");
    }

    @Override
    public void onCommand(MessageContextInteractionEvent event) {
        MessageHandler.sendMapPreview(event.getTarget(), event.getChannel());
        event.getHook().deleteOriginal().queue();
    }
}
