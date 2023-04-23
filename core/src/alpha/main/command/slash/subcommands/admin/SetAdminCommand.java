package alpha.main.command.slash.subcommands.admin;

import java.util.List;

import alpha.main.command.SlashSubcommand;
import alpha.main.data.user.GuildCache;
import alpha.main.handler.GuildHandler;
import alpha.main.handler.MessageHandler;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class SetAdminCommand extends SlashSubcommand {

    public SetAdminCommand() {
        super("setadmin", "<command.command_set_admin>[Set bot admin role to discord role]");
        addCommandOption(OptionType.ROLE, "role", "<command.admin_role>[Admin role]", true);
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        OptionMapping roleOption = event.getOption("role");
        if (roleOption == null)
            return;

        Role role = roleOption.getAsRole();
        String roleId = role.getId();

        GuildCache guildData = GuildHandler.getGuild(event.getGuild());
        if (guildData == null)
            throw new IllegalStateException("Guild data not found with <" + event.getGuild() + ">");

        List<String> adminRoleIds = guildData.getData().getAdminRoleId();

        if (adminRoleIds.contains(roleId)) {
            if (adminRoleIds.remove(roleId))
                MessageHandler.replyTranslate(event.getHook(), "<command.delete_successfully>[Delete successfully]",
                        30);
            else
                MessageHandler.replyTranslate(event.getHook(), "<command.delete_failed>[Delete failed]", 30);
        } else {
            if (adminRoleIds.add(roleId))
                MessageHandler.replyTranslate(event.getHook(), "<command.add_successfully>[Add successfully]", 30);
            else
                MessageHandler.replyTranslate(event.getHook(), "<command.add_failed>[Add failed]", 30);
        }
    }
}
