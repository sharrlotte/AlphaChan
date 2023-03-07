package AlphaChan.main.context;

import AlphaChan.main.handler.MessageHandler;
import AlphaChan.main.util.SimpleBotContextMenu;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;

public class PostSchemContextMenu extends SimpleBotContextMenu {
    public PostSchemContextMenu() {
        super("Post Schematic");
    }

    @Override
    protected void runCommand(MessageContextInteractionEvent event) {
        MessageHandler.sendSchematicPreview(event.getTarget());
        event.getHook().deleteOriginal().queue();
    }
}
