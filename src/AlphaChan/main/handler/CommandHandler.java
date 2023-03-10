package AlphaChan.main.handler;

import org.jetbrains.annotations.NotNull;

import AlphaChan.main.command.ConsoleCommandEvent;
import AlphaChan.main.command.SimpleBotCommand;
import AlphaChan.main.command.SimpleBotContextMenu;
import AlphaChan.main.command.SimpleConsoleCommand;
import AlphaChan.main.command.console.HelpConsole;
import AlphaChan.main.command.console.ReloadConfigConsole;
import AlphaChan.main.command.console.ReloadSlashCommandConsole;
import AlphaChan.main.command.console.SaveConfigConsole;
import AlphaChan.main.command.console.SetConfigConsole;
import AlphaChan.main.command.console.ShowConfigConsole;
import AlphaChan.main.command.console.ShowGuildConsole;
import AlphaChan.main.command.console.ShowUserConsole;
import AlphaChan.main.command.context.DeleteMessageContextMenu;
import AlphaChan.main.command.context.PostMapContextMenu;
import AlphaChan.main.command.context.PostSchemContextMenu;
import AlphaChan.main.command.slash.AdminCommand;
import AlphaChan.main.command.slash.BotCommand;
import AlphaChan.main.command.slash.MindustryCommand;
import AlphaChan.main.command.slash.UserCommand;
import AlphaChan.main.command.slash.YuiCommand;

import AlphaChan.main.util.Log;
import AlphaChan.main.util.StringUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.Collection;
import java.util.HashMap;

import javax.annotation.Nonnull;

import static AlphaChan.AlphaChan.*;

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

    public static CommandHandler getInstance() {
        if (instance == null)
            instance = new CommandHandler();
        return instance;
    }

    public static void updateCommand() {
        jda.updateCommands().complete();

        SlashCommandHandler.getCommands().forEach(c -> {
            jda.upsertCommand(c.command).complete();
            Log.system("Added command " + c.getName());
        });

        ContextMenuHandler.getCommands().forEach(c -> {
            jda.upsertCommand(c.command).complete();
            Log.system("Added command " + c.getName());
        });
    }

    public class SlashCommandHandler extends ListenerAdapter {

        private static HashMap<String, SimpleBotCommand> slashCommands;

        private void init() {

            slashCommands = new HashMap<>();
            addCommand(new AdminCommand());
            addCommand(new BotCommand());
            addCommand(new MindustryCommand());
            addCommand(new UserCommand());
            addCommand(new YuiCommand());

            jda.addEventListener(this);
            Log.system("Slash command handler up");
        }

        public static Collection<SimpleBotCommand> getCommands() {
            return slashCommands.values();
        }

        public static HashMap<String, SimpleBotCommand> getCommandMap() {
            return slashCommands;
        }

        public static void addCommand(SimpleBotCommand command) {
            slashCommands.put(command.getName(), command);
        }

        public static void registerCommand(Guild guild) {
            for (SimpleBotCommand command : slashCommands.values()) {
                guild.upsertCommand(command.command).queue();
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
        public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
            try {
                event.deferReply().queue();
                handleCommand(event);
            } catch (Exception e) {
                Log.error(e);
            }
        }

        @Override
        public void onCommandAutoCompleteInteraction(@Nonnull CommandAutoCompleteInteractionEvent event) {
            String command = event.getName();

            if (slashCommands.containsKey(command))
                slashCommands.get(command).onAutoComplete(event);
        }

        public static void handleCommand(@NotNull SlashCommandInteractionEvent event) {
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
                    Log.print("LOG", MessageHandler.getMessageSender(event) + ": used " + event.getName() + " "
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

        private static boolean isRunning = true;

        private static String command = new String();
        private static final String SEPARATOR = " ";

        private static HashMap<String, SimpleConsoleCommand> consoleCommands;

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

            UpdatableHandler.run("CONSOLE", 0, () -> run());
            Log.system("Console command handler up");
        }

        public static Collection<String> getCommands() {
            return consoleCommands.keySet();
        }

        public static SimpleConsoleCommand getCommand(String name) {
            return consoleCommands.get(name);
        }

        public static void addCommand(SimpleConsoleCommand command) {
            consoleCommands.put(command.getName(), command);
        }

        public static void run() {
            isRunning = true;
            while (isRunning) {
                command = System.console().readLine();
                runCommand(command);
            }
        }

        public static void kill() {
            isRunning = false;
        }

        private static void runCommand(String command) {

            if (command == null)
                return;

            if (command.isBlank())
                return;

            if (command.charAt(0) != '/')
                return;

            command = command.substring(1, command.length());
            String[] field = command.split(SEPARATOR);

            if (consoleCommands.containsKey(field[0])) {
                consoleCommands.get(field[0]).onCommand(new ConsoleCommandEvent(command));

            } else {

                String estimate = StringUtils.findBestMatch(command, consoleCommands.keySet());

                if (estimate == null) {
                    Log.info("COMMAND NOT FOUND",
                            "[/" + command + "] doesn't exists, use [/help] to get all command");
                } else {
                    Log.info("COMMAND NOT FOUND",
                            "[/" + command + "] doesn't exists, do you mean [/" + estimate + "]");
                }
            }
        }

    }

    public final class ContextMenuHandler extends ListenerAdapter {

        private static HashMap<String, SimpleBotContextMenu> contextCommands;

        private void init() {

            contextCommands = new HashMap<>();
            addCommand(new PostMapContextMenu());
            addCommand(new PostSchemContextMenu());
            addCommand(new DeleteMessageContextMenu());

            jda.addEventListener(this);
            Log.system("Context menu handler up");

        }

        public static Collection<SimpleBotContextMenu> getCommands() {
            return contextCommands.values();
        }

        public static void addCommand(SimpleBotContextMenu command) {
            contextCommands.put(command.getName(), command);
        }

        @Override
        public void onMessageContextInteraction(@Nonnull MessageContextInteractionEvent event) {
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

            if (!botMember.hasPermission(Permission.ADMINISTRATOR)) {
                MessageHandler.replyMessage(event, "Vui lòng cho bot vai trò người quản lí để sử dụng bot", 30);
                return;
            }

            if (contextCommands.containsKey(command)) {
                contextCommands.get(command).onCommand(event);
                Log.print("LOG", MessageHandler.getMessageSender(event.getTarget()) + ": used " + event.getName());
            }

        }
    }
}
