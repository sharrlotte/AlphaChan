package alpha.main.command;

import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public abstract class ContextMenuCommand {

    private String name;

    public CommandData command;

    public ContextMenuCommand(String name) {
        this.name = name;
        command = Commands.message(name);
    }

    public String getName() {
        return this.name;
    }

    public abstract void onCommand(MessageContextInteractionEvent event);

    public void runCommand(MessageContextInteractionEvent event) {
        onCommand(event);
    }

}
