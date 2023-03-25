package AlphaChan.main.command.context;

import AlphaChan.main.command.ContextMenu;
import AlphaChan.main.handler.MessageHandler;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;

public class PostSchemContextMenu extends ContextMenu {
    public PostSchemContextMenu() {
        super("Post Schematic");
    }

    @Override
    public void runCommand(MessageContextInteractionEvent event) {
        MessageHandler.sendSchematicPreview(event.getTarget());
        event.getHook().deleteOriginal().queue();
    }
}
