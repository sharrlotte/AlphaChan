package AlphaChan.main.command.slash.subcommands.admin;

import java.util.List;

import AlphaChan.main.command.SlashSubcommand;
import AlphaChan.main.data.user.GuildCache;
import AlphaChan.main.handler.GuildHandler;
import AlphaChan.main.handler.MessageHandler;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class SetAdminCommand extends SlashSubcommand {

    public SetAdminCommand() {
        super("setadmin", "<?command.command_set_admin>");
        this.addOption(OptionType.ROLE, "role", "<?command.admin_role>", true);
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
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
                MessageHandler.reply(event, "<?command.delete_successfully>", 30);
            else
                MessageHandler.reply(event, "<?command.delete_failed>", 30);
        } else {
            if (adminRoleIds.add(roleId))
                MessageHandler.reply(event, "<?command.add_successfully>", 30);
            else
                MessageHandler.reply(event, "<?command.add_failed>", 30);
        }
    }
}
