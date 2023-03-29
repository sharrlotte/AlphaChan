package AlphaChan.main.command;

import AlphaChan.main.handler.LocaleManager;
import AlphaChan.main.handler.MessageHandler;
import AlphaChan.main.handler.UpdatableHandler;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public abstract class SlashSubcommand extends SubcommandData {

    protected final int MAX_OPTIONS = 10;
    private boolean isThreaded = false;
    private boolean updateMessage = false;

    public SlashSubcommand(String name, String description) {
        this(name, description, false, false);
    }

    public SlashSubcommand(String name, String description, boolean isThreaded, boolean updateMessage) {
        super(LocaleManager.format(DiscordLocale.ENGLISH_US, name), description);
        this.isThreaded = isThreaded;
        this.updateMessage = updateMessage;
    }

    public String getHelpString() {
        StringBuilder builder = new StringBuilder();
        builder.append("<?command.command>: " + getName());
        for (OptionData option : getOptions()) {
            builder.append(String.format("\n\t<%s>: %s", option.getName(), option.getDescription()));
        }

        return builder.toString();
    }

    // Override
    public void onCommand(SlashCommandInteractionEvent event) {
        if (updateMessage)
            MessageHandler.reply(event, "<?command.updating>", 60);

        if (this.isThreaded)
            UpdatableHandler.run(name, 0, () -> runCommand(event));
        else
            runCommand(event);
    }

    // Override
    public abstract void runCommand(SlashCommandInteractionEvent command);

    // Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {

    }
}
