package alpha.main.command.slash.subcommands.bot;

import static alpha.main.AlphaChan.*;

import java.util.HashMap;
import java.util.List;

import alpha.main.command.SlashCommand;
import alpha.main.command.SlashSubcommand;
import alpha.main.data.user.GuildCache;
import alpha.main.handler.GuildHandler;
import alpha.main.handler.MessageHandler;
import alpha.main.ui.discord.PageTable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

public class GuildCommand extends SlashSubcommand {

    private final int MAX_DISPLAY = 7;

    public GuildCommand() {
        super("guild", "<command.command_guild>[Show guild information]", false, false);
        this.addOption(OptionType.STRING, "guild", "<command.guild_name>[Guild want to show]", false, true);
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
                field.append("<command.guild_name>[Guild name]: " + guild.getName() + "\n");
                if (owner != null)
                    field.append("<command.guild_owner>[Guild owner]: " + owner.getEffectiveName() + "\n");
                field.append("<command.guild_total_member>[Members]: " + guild.getMemberCount());

                if (i % MAX_DISPLAY == MAX_DISPLAY - 1) {
                    builder.addField("<command.guild>[Guild]", field.toString(), false);
                    table.addPage(builder);
                    builder.clear();
                    field = new StringBuilder();
                }
            }
            table.addPage(builder);
            table.addButton(table.button("<<<", ButtonStyle.PRIMARY, () -> table.firstPage()));
            table.addButton(table.button("<", ButtonStyle.PRIMARY, () -> table.previousPage()));
            table.addButton(table.button("X", ButtonStyle.DANGER, () -> table.deleteTable()));
            table.addButton(table.button(">", ButtonStyle.PRIMARY, () -> table.nextPage()));
            table.addButton(table.button(">>>", ButtonStyle.PRIMARY, () -> table.lastPage()));

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
                field.append("<command.guild_owner>[Guild owner]: " + owner.getEffectiveName() + "\n");

            field.append("<command.guild_total_member>[Members]: " + guild.getMemberCount() + "\n");

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

            builder.addField("<command.guild_basic_information>[Guild basic information]:", field.toString(), false);
            MessageHandler.replyEmbed(event.getHook(), builder, 30);
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
