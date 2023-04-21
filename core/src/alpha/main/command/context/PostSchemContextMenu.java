package alpha.main.command.context;

import alpha.main.command.ContextMenuCommand;
import alpha.main.handler.MessageHandler;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;

public class PostSchemContextMenu extends ContextMenuCommand {
    public PostSchemContextMenu() {
        super("Post Schematic");
    }

    @Override
    public void runCommand(MessageContextInteractionEvent event) {
        MessageHandler.sendSchematicPreview(event.getTarget());
        event.getHook().deleteOriginal().queue();
    }
}
