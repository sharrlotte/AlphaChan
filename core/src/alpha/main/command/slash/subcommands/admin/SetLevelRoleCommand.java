package alpha.main.command.slash.subcommands.admin;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.HashMap;

import alpha.main.command.SlashCommand;
import alpha.main.command.SlashSubcommand;
import alpha.main.data.user.GuildCache;
import alpha.main.handler.GuildHandler;
import alpha.main.handler.MessageHandler;

public class SetLevelRoleCommand extends SlashSubcommand {
    public SetLevelRoleCommand() {
        super("setlevelrole", "<command.command_level_role>[At a auto level role when user reach level]");
        addOption(OptionType.ROLE, "role", "<command.level_role>[The role to assign]", true);
        addOption(OptionType.INTEGER, "level", "<command.required_level>[Required level]", true);
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        OptionMapping roleOption = event.getOption("role");
        if (roleOption == null)
            throw new IllegalStateException("Invalid option");

        String roleId = roleOption.getAsRole().getId();
        Guild guild = event.getGuild();
        if (guild == null)
            throw new IllegalStateException("No guild found");

        OptionMapping levelOption = event.getOption("level");
        if (levelOption == null)
            throw new IllegalStateException("Invalid option");

        int level = levelOption.getAsInt();
        GuildCache guildData = GuildHandler.getGuild(guild);
        if (guildData == null)
            throw new IllegalStateException("No guild data found");

        if (level <= -1) {
            if (guildData.removeLevelRole(roleId))
                MessageHandler.replyTranslate(event.getHook(), "<command.delete_successfully>[Delete successfully]",
                        30);
            else
                MessageHandler.replyTranslate(event.getHook(), "<command.delete_failed>[Delete failed]", 30);
        } else {
            if (guildData.addLevelRole(roleId, level))
                MessageHandler.replyTranslate(event.getHook(), "<command.add_successfully>[Add successfully]", 30);
            else
                MessageHandler.replyTranslate(event.getHook(), "<command.add_failed>[Add failed]", 30);
        }
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        String focus = event.getFocusedOption().getName();
        if (focus.equals("type")) {
            Guild guild = event.getGuild();
            if (guild == null)
                return;

            GuildCache guildData = GuildHandler.getGuild(guild);
            HashMap<String, String> options = new HashMap<String, String>();
            guildData.getData().getLevelRoleId().keySet().forEach(t -> {
                if (t == null)
                    return;
                Role role = guild.getRoleById(t);
                if (role == null)
                    return;
                options.put(role.getName() + " lv" + guildData.getData().getLevelRoleId().get(t), t);
            });
            SlashCommand.sendAutoComplete(event, options);
        }
    }

}
