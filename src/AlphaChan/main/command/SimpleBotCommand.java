package AlphaChan.main.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class SimpleBotCommand {

    public SlashCommandData command;
    public HashMap<String, SimpleBotSubcommand> subcommands = new HashMap<>();

    private final int MAX_OPTIONS = 10;

    public SimpleBotCommand(@Nonnull String name, String description) {
        command = Commands.slash(name, description);
    }

    public String getName() {
        return this.command.getName();
    }

    public String getDescription() {
        return this.command.getDescription();
    }

    // Override
    public String getHelpString() {
        return "";
    }

    // Can be overridden
    public String getHelpString(String subCommand) {
        if (!subcommands.containsKey(subCommand))
            return "Không tìm thấy lệnh " + subCommand;
        return subcommands.get(subCommand).getHelpString();
    }

    // Can be overridden
    public void onCommand(SlashCommandInteractionEvent event) {
        runCommand(event);
    }

    protected void runCommand(SlashCommandInteractionEvent event) {
        if (subcommands.containsKey(event.getSubcommandName()))
            subcommands.get(event.getSubcommandName()).onCommand(event);
        else
            reply(event, "Lệnh sai rồi kìa baka", 10);
    }

    public SimpleBotSubcommand addSubcommands(SimpleBotSubcommand subcommand) {
        subcommands.put(subcommand.getName(), subcommand);
        command.addSubcommands(subcommand);
        return subcommand;
    }

    // Auto complete handler
    public void sendAutoComplete(@Nonnull CommandAutoCompleteInteractionEvent event, HashMap<String, String> list) {
        if (list.isEmpty()) {
            sendAutoComplete(event, "Không tìm thấy kết quả khớp");
            return;
        }
        String focusString = event.getFocusedOption().getValue().toLowerCase();
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
            sendAutoComplete(event, "Không tìm thấy kết quả khớp");
            return;
        }
        event.replyChoices(options).queue();
    }

    public void sendAutoComplete(@Nonnull CommandAutoCompleteInteractionEvent event, String value) {
        sendAutoComplete(event, value, value);
    }

    public void sendAutoComplete(@Nonnull CommandAutoCompleteInteractionEvent event, String name, String value) {
        if (value.isBlank())
            event.replyChoice("Không tìm thấy kết quả khớp", "Không tìm thấy kết quả khớp").queue();
        else
            event.replyChoice(name, value).queue();
    }

    // Can be overridden
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        if (subcommands.containsKey(event.getSubcommandName())) {
            subcommands.get(event.getSubcommandName()).onAutoComplete(event);
        }
    }

    protected void replyEmbed(SlashCommandInteractionEvent event, EmbedBuilder builder, int deleteAfter) {
        event.getHook().sendMessageEmbeds(builder.build()).queue(_message -> _message.delete().queueAfter(deleteAfter, TimeUnit.SECONDS));
    }

    protected void reply(SlashCommandInteractionEvent event, String content, int deleteAfter) {
        event.getHook().sendMessage("```" + content + "```").queue(_message -> _message.delete().queueAfter(deleteAfter, TimeUnit.SECONDS));
    }

}
