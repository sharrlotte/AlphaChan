package AlphaChan.main.command.slash.subcommands.bot;

import java.util.HashMap;
import java.util.List;

import AlphaChan.main.command.SlashCommand;
import AlphaChan.main.command.SlashSubcommand;
import AlphaChan.main.data.user.GuildCache;
import AlphaChan.main.gui.discord.PageTable;
import AlphaChan.main.handler.GuildHandler;
import AlphaChan.main.handler.MessageHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static AlphaChan.AlphaChan.*;

public class GuildCommand extends SlashSubcommand {

    private final int MAX_DISPLAY = 7;

    public GuildCommand() {
        super("guild", "<?command.command_guild>", false, false);
        this.addOption(OptionType.STRING, "guild", "<?command.guild_name>", false, true);
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        OptionMapping guildOption = event.getOption("guild");
        if (guildOption == null) {
            // Show all guild
            List<Guild> guilds = jda.getGuilds();
            EmbedBuilder builder = new EmbedBuilder();
            StringBuilder field = new StringBuilder();
            PageTable table = new PageTable(event, 2);

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
            table.addButton(table.primary("<<<", () -> table.firstPage()));
            table.addButton(table.primary("<", () -> table.previousPage()));
            table.addButton(table.deny("X", () -> table.deleteTable()));
            table.addButton(table.primary(">", () -> table.nextPage()));
            table.addButton(table.primary(">>>", () -> table.lastPage()));

            table.sendTable();

        } else {
            // Get the guild base on name
            String guildId = guildOption.getAsString();
            Guild guild = jda.getGuildById(guildId);
            if (guild == null)
                return;

            GuildCache guildData = GuildHandler.getGuild(guild);
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
            for (String roleId : guildData.getData().getLevelRoleId().keySet()) {
                if (roleId == null)
                    return;

                Role role = guild.getRoleById(roleId);
                if (role != null)
                    roleString += role.getAsMention() + ", ";
            }

            if (!roleString.isEmpty())
                field.append("\n" + roleString.substring(0, roleString.length() - 2));

            builder.addField("Thông tin cơ bản:", field.toString(), false);
            MessageHandler.replyEmbed(event, builder, 30);
        }
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        String focus = event.getFocusedOption().getName();
        if (focus.equals("guild")) {
            HashMap<String, String> options = new HashMap<String, String>();
            jda.getGuilds().forEach(t -> options.put(t.getName(), t.getId()));
            SlashCommand.sendAutoComplete(event, options);

        }
    }
}
