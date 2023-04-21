package alpha.main.command;

import alpha.main.handler.LocaleManager;
import alpha.main.handler.MessageHandler;
import alpha.main.handler.UpdatableHandler;
import alpha.main.util.StringUtils;
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
        this(name, LocaleManager.format(DiscordLocale.ENGLISH_US, description), false, false);
    }

    public SlashSubcommand(String name, String description, boolean isThreaded, boolean updateMessage) {
        super(name, LocaleManager.format(DiscordLocale.ENGLISH_US, description));
        this.isThreaded = isThreaded;
        this.updateMessage = updateMessage;
    }

    public String getHelpString() {
        StringBuilder builder = new StringBuilder();
        for (OptionData option : getOptions()) {
            builder.append(String.format("\n\t%s: %s %s", //
                    option.getName(), //
                    option.getDescription(), //
                    option.isRequired() ? "(<command.required>[Required])" : ""));
        }

        return builder.toString();
    }

    // Override
    public void onCommand(SlashCommandInteractionEvent event) {
        if (updateMessage)
            MessageHandler.replyTranslate(event.getHook(), StringUtils.backtick("<command.updating>[Updating]"), 60);

        if (this.isThreaded)
            UpdatableHandler.run(getName(), 0, () -> runCommand(event));
        else
            runCommand(event);
    }

    // Override
    public abstract void runCommand(SlashCommandInteractionEvent command);

    // Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {

    }
}
