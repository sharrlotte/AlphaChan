package AlphaChan.main.command;

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

    public void onCommand(MessageContextInteractionEvent event) {
        runCommand(event);
    }

    public abstract void runCommand(MessageContextInteractionEvent event);
}
