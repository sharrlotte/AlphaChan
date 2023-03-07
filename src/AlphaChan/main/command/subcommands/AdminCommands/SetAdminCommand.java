package AlphaChan.main.command.subcommands.AdminCommands;

import AlphaChan.main.handler.GuildHandler;
import AlphaChan.main.user.GuildData;
import AlphaChan.main.util.SimpleBotSubcommand;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class SetAdminCommand extends SimpleBotSubcommand {

    public SetAdminCommand() {
        super("setadmin", "Cài đặt vai trò admin cho máy chủ");
        this.addOption(OptionType.ROLE, "role", "Vai trò admin", true);
    }

    @Override
    public String getHelpString() {
        return "Cài đặt vai trò của máy chủ:\n\t<role>: vai trò admin\n\tThêm lần nữa để xóa";
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        OptionMapping roleOption = event.getOption("role");
        if (roleOption == null)
            return;

        Role role = roleOption.getAsRole();
        String roleId = role.getId();

        GuildData guildData = GuildHandler.getGuild(event.getGuild());
        if (guildData == null)
            throw new IllegalStateException("Guild data not found with <" + event.getGuild() + ">");

        if (guildData.adminRoleId.contains(roleId)) {
            if (guildData.adminRoleId.remove(roleId))
                reply(event, "Xóa vai trò thành công", 30);
            else
                reply(event, "Xóa vai trò thất bại", 30);
        } else {
            if (guildData.adminRoleId.add(roleId))
                reply(event, "Thêm vai trò thành công", 30);
            else
                reply(event, "Thêm vai trò thất bại", 30);
        }
    }
}
