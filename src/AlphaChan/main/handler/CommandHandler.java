package AlphaChan.main.handler;

import org.jetbrains.annotations.NotNull;

import AlphaChan.main.command.AdminCommand;
import AlphaChan.main.command.BotCommand;
import AlphaChan.main.command.MindustryCommand;
import AlphaChan.main.command.YuiCommand;
import AlphaChan.main.command.UserCommand;
import AlphaChan.main.util.Log;
import AlphaChan.main.util.SimpleBotCommand;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import static AlphaChan.AlphaChan.*;

public class CommandHandler extends ListenerAdapter {
    private static CommandHandler instance = new CommandHandler();

    private static HashMap<String, SimpleBotCommand> commands;

    private CommandHandler() {

        commands = new HashMap<>();
        addCommand(new AdminCommand());
        addCommand(new BotCommand());
        addCommand(new MindustryCommand());
        addCommand(new UserCommand());
        addCommand(new YuiCommand());

        jda.addEventListener(this);
        Log.system("Command handler up");
    }

    public static CommandHandler getInstance() {
        if (instance == null)
            instance = new CommandHandler();
        return instance;
    }

    public static Collection<SimpleBotCommand> getCommands() {
        return commands.values();
    }

    public static HashMap<String, SimpleBotCommand> getCommandHashMap() {
        return commands;
    }

    public static void addCommand(SimpleBotCommand command) {
        commands.put(command.getName(), command);
    }

    public static void registerCommand(Guild guild) {
        for (SimpleBotCommand command : commands.values()) {
            guild.upsertCommand(command.command).queue();
        }
    }

    public static void unregisterCommand(Guild guild) {
        guild.retrieveCommands().queue(commands -> {
            for (Command command : commands) {
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

        if (commands.containsKey(command))
            commands.get(command).onAutoComplete(event);
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

            if (commands.containsKey(command)) {
                // Call subcommand
                commands.get(command).onCommand(event);
                // Print to terminal
                Log.print("LOG", MessageHandler.getMessageSender(event) + ": used " + event.getName() + " "
                        + event.getSubcommandName() + " " + event.getOptions().toString());
                // Send to discord log channel
                if (!command.equals("yui")) {
                    MessageHandler.log(guild,
                            member.getEffectiveName() + " đã sử dụng " + command + " " + event.getSubcommandName());
                }
            } else
                reply(event, "Lệnh sai rồi kìa baka", 10);
        } catch (Exception e) {
            Log.error(e);
        }

    }

    public static void replyEmbed(SlashCommandInteractionEvent event, EmbedBuilder builder, int deleteAfter) {
        event.getHook().sendMessageEmbeds(builder.build())
                .queue(_message -> _message.delete().queueAfter(deleteAfter, TimeUnit.SECONDS));
    }

    public static void reply(SlashCommandInteractionEvent event, String content, int deleteAfter) {
        event.getHook().sendMessage("```" + content + "```")
                .queue(_message -> _message.delete().queueAfter(deleteAfter, TimeUnit.SECONDS));
    }
}
