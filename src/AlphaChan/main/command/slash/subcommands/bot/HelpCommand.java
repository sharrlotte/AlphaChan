package AlphaChan.main.command.slash.subcommands.bot;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.HashMap;

import AlphaChan.main.command.SlashCommand;
import AlphaChan.main.command.SlashSubcommand;
import AlphaChan.main.handler.MessageHandler;
import AlphaChan.main.handler.CommandHandler.SlashCommandHandler;

public class HelpCommand extends SlashSubcommand {
    public HelpCommand() {
        super("help", "<@command.command_help>");
        addOption(OptionType.STRING, "command", "<@command.command_name>", true, true);
        addOption(OptionType.STRING, "subcommand", "<@command.subcommand_name>", true, true);
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        OptionMapping commandOption = event.getOption("command");
        if (commandOption == null)
            return;
        OptionMapping subcommandOption = event.getOption("subcommand");
        if (subcommandOption == null)
            return;
        String command = commandOption.getAsString();
        String subcommand = subcommandOption.getAsString();
        if (SlashCommandHandler.getCommandMap().containsKey(command))
            MessageHandler.reply(event,
                    "/" + command + " " + subcommand + ":" + SlashCommandHandler.getCommandMap().get(command).getHelpString(subcommand),
                    60);
        else
            MessageHandler.reply(event, "Lệnh " + command + " " + subcommand + " không tồn tại", 10);
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        String focus = event.getFocusedOption().getName();
        if (focus.equals("command")) {
            HashMap<String, String> options = new HashMap<String, String>();
            SlashCommandHandler.getCommandMap().keySet().forEach(t -> options.put(t, t));
            SlashCommand.sendAutoComplete(event, options);

        } else if (focus.equals("subcommand")) {
            OptionMapping commandOption = event.getOption("command");
            if (commandOption == null)
                return;
            String command = commandOption.getAsString();
            SlashCommand subcommands = SlashCommandHandler.getCommandMap().get(command);
            if (subcommands == null)
                return;
            HashMap<String, String> options = new HashMap<String, String>();
            subcommands.getSubcommands().forEach(t -> options.put(t.getName(), t.getName()));
            SlashCommand.sendAutoComplete(event, options);

        }
    }
}
