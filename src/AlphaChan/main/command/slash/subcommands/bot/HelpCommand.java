package AlphaChan.main.command.slash.subcommands.bot;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.HashMap;

import AlphaChan.main.command.SimpleBotCommand;
import AlphaChan.main.command.SimpleBotSubcommand;
import AlphaChan.main.handler.CommandHandler.SlashCommandHandler;

public class HelpCommand extends SimpleBotSubcommand {
    public HelpCommand() {
        super("help", "Hiển thị thông tin các lệnh");
        this.addOption(OptionType.STRING, "command", "Tên lệnh", true, true).//
                addOption(OptionType.STRING, "subcommand", "Tên lệnh", true, true);
    }

    @Override
    public String getHelpString() {
        return "Help";
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
            reply(event, "/" + command + " " + subcommand + "\n"
                    + SlashCommandHandler.getCommandMap().get(command).getHelpString(subcommand), 60);
        else
            reply(event, "Lệnh " + command + " " + subcommand + " không tồn tại", 10);
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        String focus = event.getFocusedOption().getName();
        if (focus.equals("command")) {
            HashMap<String, String> options = new HashMap<String, String>();
            SlashCommandHandler.getCommandMap().keySet().forEach(t -> options.put(t, t));
            sendAutoComplete(event, options);

        } else if (focus.equals("subcommand")) {
            OptionMapping commandOption = event.getOption("command");
            if (commandOption == null)
                return;
            String command = commandOption.getAsString();
            SimpleBotCommand subcommands = SlashCommandHandler.getCommandMap().get(command);
            if (subcommands == null)
                return;
            HashMap<String, String> options = new HashMap<String, String>();
            subcommands.subcommands.keySet().forEach(t -> options.put(t, t));
            sendAutoComplete(event, options);

        }
    }
}
