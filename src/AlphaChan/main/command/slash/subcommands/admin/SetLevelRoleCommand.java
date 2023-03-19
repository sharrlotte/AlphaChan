package AlphaChan.main.command.slash.subcommands.admin;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.HashMap;

import AlphaChan.main.command.SimpleBotSubcommand;
import AlphaChan.main.data.user.GuildData;
import AlphaChan.main.handler.GuildHandler;

public class SetLevelRoleCommand extends SimpleBotSubcommand {
    public SetLevelRoleCommand() {
        super("setlevelrole", "Cài đặt các vai trò của máy chủ");
        addOption(OptionType.ROLE, "role", "Vai trò muốn gán", true);
        addOption(OptionType.INTEGER, "level", "Cấp độ cần thiết để nhận vai trò", true);
    }

    @Override
    public String getHelpString() {
        return "Cài đặt các vai trò của máy chủ:\n\t<type>: loại vai trò muốn đặt\n\t<role>: vai trò muốn gán\n\t<level>: cấp độ cần thiết để có được vai trò(-1 để xóa)";
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
        GuildData guildData = GuildHandler.getGuild(guild);
        if (guildData == null)
            throw new IllegalStateException("No guild data found");
        if (level <= -1) {
            if (guildData._removeRole(roleId))
                reply(event, "Xóa vai trò thành công", 30);
            else
                reply(event, "Xóa vai trò thất bại", 30);
        } else {
            if (guildData._addRole(roleId, level))
                reply(event, "Thêm vai trò thành công", 30);
            else
                reply(event, "Thêm vai trò thất bại", 30);
        }
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        String focus = event.getFocusedOption().getName();
        if (focus.equals("type")) {
            Guild guild = event.getGuild();
            if (guild == null)
                return;

            GuildData guildData = GuildHandler.getGuild(guild);
            HashMap<String, String> options = new HashMap<String, String>();
            guildData.levelRoleId.keySet().forEach(t -> {
                if (t == null)
                    return;
                Role role = guild.getRoleById(t);
                if (role == null)
                    return;
                options.put(role.getName() + "     lv" + guildData.levelRoleId.get(t), t);
            });
            sendAutoComplete(event, options);
        }
    }

}
