package alpha.main.command.slash.subcommands.bot;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.HashMap;

import alpha.main.command.SlashCommand;
import alpha.main.command.SlashSubcommand;
import alpha.main.handler.LocaleManager;
import alpha.main.handler.MessageHandler;
import alpha.main.handler.CommandHandler.SlashCommandHandler;
import alpha.main.util.StringUtils;

public class HelpCommand extends SlashSubcommand {
    public HelpCommand() {
        super("help", "<command.command_help>[Help command]");
        addOption(OptionType.STRING, "command", "<command.command_name>[Command name]", false, true);
        addOption(OptionType.STRING, "subcommand", "<command.subcommand_name>[Subcommand name]", false, true);
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
            MessageHandler.reply(event.getHook(), StringUtils.backtick(
                    "/" + command + " " + subcommand + ":" + LocaleManager.format(event.getGuild(),
                            SlashCommandHandler.getCommandMap().get(command).getHelpString(subcommand))),
                    60);
        else
            MessageHandler.reply(event.getHook(), LocaleManager.format(event.getGuild(),
                    "<command.command_not_exist>[/%s %s doesn't exists]", command, subcommand), 10);
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
            if (commandOption == null) {
                SlashCommand.sendAutoComplete(event, "<command.no_command_provided>[No command group]");
                return;
            }
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
