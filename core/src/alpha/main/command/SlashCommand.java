package alpha.main.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import alpha.main.handler.LocaleManager;
import alpha.main.handler.MessageHandler;
import alpha.main.util.Log;
import alpha.main.util.StringUtils;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

public abstract class SlashCommand extends CommandDataImpl {

    private static final int MAX_OPTIONS = 10;

    public SlashCommand(String name, String description) {
        super(LocaleManager.format(DiscordLocale.ENGLISH_US, name), description);
    }

    public String getHelpString() {
        StringBuilder builder = new StringBuilder();
        builder.append("<command.command>[Command name]: " + getName());
        for (SubcommandData subcommand : getSubcommands()) {
            builder.append("\n\t: " + subcommand.getName());
        }

        return builder.toString();
    }

    public String getHelpString(String name) {
        SubcommandData subcommand = getSubcommand(name);
        if (subcommand != null && subcommand instanceof SlashSubcommand slashSubcommand)
            return slashSubcommand.getHelpString();

        return "<command.command_not_found>[Command not found]" + name;
    }

    // Can be overridden
    public void onCommand(SlashCommandInteractionEvent event) {
        runCommand(event);
    }

    public SubcommandData getSubcommand(String name) {
        for (SubcommandData subcommandData : getSubcommands()) {
            if (subcommandData.getName().equals(name))
                return subcommandData;
        }
        return null;
    }

    protected void runCommand(SlashCommandInteractionEvent event) {

        try {

            SubcommandData subcommand = getSubcommand(event.getSubcommandName());
            if (subcommand == null) {
                MessageHandler.replyTranslate(event.getHook(),
                        StringUtils.backtick("<command.command_not_found>[Error: Not found command]"), 10);
                return;
            }
            if (subcommand instanceof SlashSubcommand slashSubcommand) {
                slashSubcommand.onCommand(event);
            }

        } catch (Exception e) {
            MessageHandler.delete(event.getHook());
            Log.error(e);
        }
    }

    // Auto complete handler
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        SubcommandData subcommand = getSubcommand(event.getSubcommandName());
        if (subcommand == null)
            return;

        if (subcommand instanceof SlashSubcommand slashSubcommand) {
            slashSubcommand.onAutoComplete(event);
        }
    }

    public static void sendAutoComplete(CommandAutoCompleteInteractionEvent event, HashMap<String, String> list) {

        String focusString = event.getFocusedOption().getValue();
        List<Command.Choice> options = new ArrayList<Command.Choice>();

        int count = 0;
        for (String name : list.keySet()) {
            if (count > MAX_OPTIONS)
                break;

            if (name.toLowerCase().contains(focusString)) {
                String value = list.get(name);
                if (value == null)
                    return;
                options.add(new Command.Choice(name, value));
                count++;
            }
        }

        if (options.isEmpty()) {
            sendAutoComplete(event, "<command.no_choices>[No matches]");
            return;
        }
        event.replyChoices(options).queue();
    }

    public static void sendAutoComplete(CommandAutoCompleteInteractionEvent event, String value) {
        sendAutoComplete(event, value, value);
    }

    public static void sendAutoComplete(CommandAutoCompleteInteractionEvent event, String name, String value) {
        if (value.isBlank())
            event.replyChoice("<command.no_choices>[No matches]", "null").queue();
        else
            event.replyChoice(name, value).queue();
    }
}
