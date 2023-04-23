package alpha.main.handler;

import alpha.main.command.ConsoleCommandEvent;
import alpha.main.command.ContextMenuCommand;
import alpha.main.command.SlashCommand;
import alpha.main.command.ConsoleAutoCompleteEvent;
import alpha.main.command.ConsoleCommand;
import alpha.main.command.console.HelpConsole;
import alpha.main.command.console.ReloadConfigConsole;
import alpha.main.command.console.ReloadSlashCommandConsole;
import alpha.main.command.console.SaveConfigConsole;
import alpha.main.command.console.SetConfigConsole;
import alpha.main.command.console.ShowConfigConsole;
import alpha.main.command.console.ShowGuildConsole;
import alpha.main.command.console.ShowUserConsole;
import alpha.main.command.console.ShutdownConsole;
import alpha.main.command.context.DeleteMessageContextMenu;
import alpha.main.command.context.PostMapContextMenu;
import alpha.main.command.context.PostSchemContextMenu;
import alpha.main.command.slash.AdminCommand;
import alpha.main.command.slash.BotCommand;
import alpha.main.command.slash.MindustryCommand;
import alpha.main.command.slash.MusicCommand;
import alpha.main.command.slash.UserCommand;
import alpha.main.command.slash.YuiCommand;
import alpha.main.ui.bot.AutoCompleteTextField;
import alpha.main.util.Log;
import alpha.main.util.StringUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;

import static alpha.main.AlphaChan.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class CommandHandler {

    private static CommandHandler instance = new CommandHandler();
    private static SlashCommandHandler slashCommandHandler;
    private static ConsoleCommandHandler consoleCommandHandler;
    private static ContextMenuHandler contextMenuHandler;

    private CommandHandler() {

        slashCommandHandler = new SlashCommandHandler();
        consoleCommandHandler = new ConsoleCommandHandler();
        contextMenuHandler = new ContextMenuHandler();

        slashCommandHandler.init();
        consoleCommandHandler.init();
        contextMenuHandler.init();
    }

    public synchronized static CommandHandler getInstance() {
        if (instance == null)
            instance = new CommandHandler();
        return instance;
    }

    public static void updateCommand() {
        jda.updateCommands().complete();

        SlashCommandHandler.getCommands().forEach(c -> {
            jda.upsertCommand(c).complete();
            Log.system("Added command " + c.getName());
        });

        ContextMenuHandler.getCommands().forEach(c -> {
            jda.upsertCommand(c.command).complete();
            Log.system("Added command " + c.getName());
        });
    }

    public class SlashCommandHandler extends ListenerAdapter {

        private static HashMap<String, SlashCommand> slashCommands;

        private void init() {

            slashCommands = new HashMap<>();
            addCommand(new AdminCommand());
            addCommand(new BotCommand());
            addCommand(new MindustryCommand());
            addCommand(new UserCommand());
            addCommand(new YuiCommand());
            addCommand(new MusicCommand());

            jda.addEventListener(this);
            Log.system("Slash command handler up");
        }

        public static Collection<SlashCommand> getCommands() {
            return slashCommands.values();
        }

        public static HashMap<String, SlashCommand> getCommandMap() {
            return slashCommands;
        }

        public static void addCommand(SlashCommand command) {
            slashCommands.put(command.getName(), command);
        }

        public static void registerCommand(Guild guild) {
            for (SlashCommand command : slashCommands.values()) {
                guild.upsertCommand(command).queue();
            }
        }

        public static void unregisterCommand(Guild guild) {
            guild.retrieveCommands().queue(slashCommands -> {
                for (Command command : slashCommands) {
                    command.delete().complete();
                }
            });
        }

        @Override
        public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
            try {
                event.deferReply().queue();
                handleCommand(event);

            } catch (Exception e) {
                Log.error(e);
            }
        }

        @Override
        public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
            String command = event.getName();

            if (slashCommands.containsKey(command))
                slashCommands.get(command).onAutoComplete(event);
        }

        public static void handleCommand(SlashCommandInteractionEvent event) {
            try {
                String command = event.getName();

                Guild guild = event.getGuild();
                if (guild == null)
                    throw new IllegalStateException("Guild not exists");

                Member botMember = guild.getMember(jda.getSelfUser());
                if (botMember == null)
                    throw new IllegalStateException("Bot not exists");

                Member member = event.getMember();
                if (member == null)
                    throw new IllegalStateException("Member not exists");

                if (slashCommands.containsKey(command)) {
                    // Call subcommand
                    slashCommands.get(command).onCommand(event);
                    // Print to terminal
                    Log.info("INTERACTION", MessageHandler.getMessageSender(event) + ": used " + event.getName() + " "
                            + event.getSubcommandName() + " " + event.getOptions().toString());
                    // Send to discord log channel
                    if (!command.equals("yui")) {
                        MessageHandler.log(guild,
                                member.getEffectiveName() + " đã sử dụng " + command + " " + event.getSubcommandName());
                    }
                }

            } catch (Exception e) {
                Log.error(e);
            }

        }
    }

    public class ConsoleCommandHandler {

        private static HashMap<String, ConsoleCommand> consoleCommands;

        private void init() {
            consoleCommands = new HashMap<>();

            addCommand(new ShowGuildConsole());
            addCommand(new ShowUserConsole());
            addCommand(new ShowConfigConsole());
            addCommand(new HelpConsole());
            addCommand(new ReloadConfigConsole());
            addCommand(new ReloadSlashCommandConsole());
            addCommand(new SetConfigConsole());
            addCommand(new SaveConfigConsole());
            addCommand(new ShutdownConsole());

            Log.system("Console command handler up");
        }

        public static Collection<String> getCommands() {
            return consoleCommands.keySet();
        }

        public static ConsoleCommand getCommand(String name) {
            return consoleCommands.get(name);
        }

        public static void addCommand(ConsoleCommand command) {
            consoleCommands.put(command.getName(), command);
        }

        public static void onCommand(String command) {

            if (!isValidConsoleCommand(command))
                return;

            ConsoleCommandEvent event = ConsoleCommandEvent.parseCommand(command);

            if (consoleCommands.containsKey(event.getCommandName())) {
                if (event != null && event.getCommandName() != null) {
                    consoleCommands.get(event.getCommandName()).onCommand(event);
                    return;
                }
            }

            String estimate = StringUtils.findBestMatch(command, consoleCommands.keySet());

            if (estimate == null) {
                Log.info("COMMAND NOT FOUND", "[" + command + "] doesn't exists, use [/help] to get all command");
            } else {
                Log.info("COMMAND NOT FOUND", "[" + command + "] doesn't exists, do you mean [/" + estimate + "]");
            }
        }

        public static void onAutoComplete(AutoCompleteTextField field) {
            if (!isValidConsoleCommand(field.getText()))
                return;

            ConsoleAutoCompleteEvent event = ConsoleAutoCompleteEvent.parseCommand(field);

            if (consoleCommands.containsKey(event.getCommandName())
                    && event.getCommandName().length() != field.getCaretPosition()) {
                if (event != null && event.getCommandName() != null) {
                    consoleCommands.get(event.getCommandName()).runAutoComplete(event);
                    return;
                }

            } else {

                ArrayList<String> entries = new ArrayList<String>(consoleCommands.keySet());
                List<String> result = StringUtils.findBestMatches(event.getCommandName(), entries, 10);
                field.replyChoices(event.getCommandName(), result//
                        .stream()//
                        .map((v) -> new AutoCompleteTextField.Choice(v, v))
                        .collect(Collectors.toList()));

            }
        }

        public static boolean isValidConsoleCommand(String command) {
            if (command == null)
                return false;

            if (command.isBlank())
                return false;

            if (command.charAt(0) != '/')
                return false;

            return true;
        }
    }

    public final class ContextMenuHandler extends ListenerAdapter {

        private static HashMap<String, ContextMenuCommand> contextCommands;

        private void init() {

            contextCommands = new HashMap<>();
            addCommand(new PostMapContextMenu());
            addCommand(new PostSchemContextMenu());
            addCommand(new DeleteMessageContextMenu());

            jda.addEventListener(this);
            Log.system("Context menu handler up");

        }

        public static Collection<ContextMenuCommand> getCommands() {
            return contextCommands.values();
        }

        public static void addCommand(ContextMenuCommand command) {
            contextCommands.put(command.getName(), command);
        }

        @Override
        public void onMessageContextInteraction(MessageContextInteractionEvent event) {
            event.deferReply().queue();
            handleCommand(event);
        }

        public static void handleCommand(MessageContextInteractionEvent event) {
            String command = event.getName();

            Guild guild = event.getGuild();
            if (guild == null)
                return;

            Member botMember = guild.getMember(jda.getSelfUser());
            if (botMember == null)
                return;

            Member member = event.getMember();
            if (member == null)
                return;

            if (contextCommands.containsKey(command)) {
                contextCommands.get(command).onCommand(event);
                Log.info("INTERACTION",
                        MessageHandler.getMessageSender(event.getTarget()) + ": used " + event.getName());
            }

        }
    }
}
