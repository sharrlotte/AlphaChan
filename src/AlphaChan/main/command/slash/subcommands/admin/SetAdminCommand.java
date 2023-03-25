package AlphaChan.main.command.slash.subcommands.admin;

import java.util.List;

import AlphaChan.main.command.SimpleBotSubcommand;
import AlphaChan.main.data.user.GuildCache;
import AlphaChan.main.handler.GuildHandler;
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

        GuildCache guildData = GuildHandler.getGuild(event.getGuild());
        if (guildData == null)
            throw new IllegalStateException("Guild data not found with <" + event.getGuild() + ">");

        List<String> adminRoleIds = guildData.getData().getAdminRoleId();

        if (adminRoleIds.contains(roleId)) {
            if (adminRoleIds.remove(roleId))
                reply(event, "Xóa vai trò thành công", 30);
            else
                reply(event, "Xóa vai trò thất bại", 30);
        } else {
            if (adminRoleIds.add(roleId))
                reply(event, "Thêm vai trò thành công", 30);
            else
                reply(event, "Thêm vai trò thất bại", 30);
        }
    }
}
