package AlphaChan.main.command.slash.subcommands.bot;

import java.util.HashMap;
import java.util.List;

import AlphaChan.main.command.SimpleBotSubcommand;
import AlphaChan.main.command.SimpleTable;
import AlphaChan.main.data.user.GuildData;
import AlphaChan.main.handler.GuildHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static AlphaChan.AlphaChan.*;

public class GuildCommand extends SimpleBotSubcommand {

    private final int MAX_DISPLAY = 7;

    public GuildCommand() {
        super("guild", "Hiển thị thông tin của máy chủ discord", false, false);
        this.addOption(OptionType.STRING, "guild", "Tên máy chủ", false, true);
    }

    @Override
    public String getHelpString() {
        return "Hiển thị thông tin của máy chủ discord mà bot đã gia nhập:\n\t<guild>: Tên máy chủ muốn xem, nếu không nhập guild thì sẽ hiện tất cả các máy chủ, ngược lại sẽ hiện thông tin máy chủ đã nhập";
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        OptionMapping guildOption = event.getOption("guild");
        if (guildOption == null) {
            // Show all guild
            List<Guild> guilds = jda.getGuilds();
            EmbedBuilder builder = new EmbedBuilder();
            StringBuilder field = new StringBuilder();
            SimpleTable table = new SimpleTable(event, 2);

            for (int i = 0; i < guilds.size(); i++) {
                Guild guild = guilds.get(i);
                Member owner = guild.getOwner();
                field.append("```Tên máy chủ: " + guild.getName() + "\n");
                if (owner != null)
                    field.append("Chủ máy chủ: " + owner.getEffectiveName() + "\n");
                field.append("Tổng số thành viên: " + guild.getMemberCount());
                field.append("```");

                if (i % MAX_DISPLAY == MAX_DISPLAY - 1) {
                    builder.addField("_Máy chủ_", field.toString(), false);
                    table.addPage(builder);
                    builder.clear();
                    field = new StringBuilder();
                }
            }
            table.addPage(builder);
            table.addButtonPrimary("<<<", () -> table.firstPage())//
                    .addButtonPrimary("<", () -> table.previousPage())//
                    .addButtonDeny("X", () -> table.delete())//
                    .addButtonPrimary(">", () -> table.nextPage())//
                    .addButtonPrimary(">>>", () -> table.lastPage());

            table.sendTable();

        } else {
            // Get the guild base on name
            String guildId = guildOption.getAsString();
            Guild guild = jda.getGuildById(guildId);
            if (guild == null)
                return;

            GuildData guildData = GuildHandler.getGuild(guild);
            EmbedBuilder builder = new EmbedBuilder();
            StringBuilder field = new StringBuilder();

            builder.setAuthor(guild.getName(), null, guild.getIconUrl());
            Member owner = guild.getOwner();
            if (owner != null)
                field.append("```Chủ máy chủ: " + owner.getEffectiveName() + "\n");
            else
                field.append("```Chủ máy chủ: Không rõ\n");

            field.append("Số thành viên: " + guild.getMemberCount() + "\n" + //
                    "```");

            builder.setDescription("Link: " + guild.getTextChannels().get(0).createInvite().complete().getUrl());

            String roleString = "";
            for (String roleId : guildData.levelRoleId.keySet()) {
                if (roleId == null)
                    return;
                Role role = guild.getRoleById(roleId);
                if (role != null)
                    roleString += role.getName() + ": " + guildData.levelRoleId.get(roleId) + "\n";
            }
            if (!roleString.isEmpty())
                field.append("\n" + roleString.substring(0, roleString.length() - 2));
            builder.addField("Thông tin cơ bản:", field.toString(), false);
            replyEmbed(event, builder, 30);
        }
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        String focus = event.getFocusedOption().getName();
        if (focus.equals("guild")) {
            HashMap<String, String> options = new HashMap<String, String>();
            jda.getGuilds().forEach(t -> options.put(t.getName(), t.getId()));
            sendAutoComplete(event, options);

        }
    }
}
