package alpha.main.command.slash.subcommands.user;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import alpha.main.command.SlashSubcommand;
import alpha.main.data.user.UserCache;
import alpha.main.data.user.UserCache.PointType;
import alpha.main.handler.MessageHandler;
import alpha.main.handler.UserHandler;

import java.util.List;
import java.awt.Color;

public class InfoCommand extends SlashSubcommand {
    public InfoCommand() {
        super("info", "<command.command_user_info>[Show user information]", true, false);
        addCommandOption(OptionType.USER, "user", "<command.user_name>[User want to show information]", false);
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        OptionMapping userOption = event.getOption("user");

        // Display command caller information
        if (userOption == null) {
            Member member = event.getMember();
            if (member == null) {
                return;
            }

            MessageHandler.replyEmbedTranslate(event.getHook(), getDisplayedUserInformation(member), 30);

        } else {
            // Display target information
            User user = userOption.getAsUser();
            Guild guild = event.getGuild();
            if (guild == null) {
                return;
            }
            Member member = guild.getMember(user);
            if (member == null) {
                return;
            }
            MessageHandler.replyEmbedTranslate(event.getHook(), getDisplayedUserInformation(member), 30);
        }
    }

    private EmbedBuilder getDisplayedUserInformation(Member member) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(member.getEffectiveName(), null, member.getEffectiveAvatarUrl());
        builder.setThumbnail(member.getEffectiveAvatarUrl());
        // Display role
        List<Role> roles = member.getRoles();
        if (roles.size() > 0) {
            String roleString = "";
            for (Role role : roles)
                roleString += role.getAsMention() + ", ";

            if (!roleString.isEmpty())
                roleString = roleString.substring(0, roleString.length() - 2);

            builder.addField("<command.role>[Role]", roleString, false);
        }

        // Display point
        UserCache user = UserHandler.getUserNoCache(member);

        builder.addField("<command.basic_info>[Basic information]",
                "<command.level>[Level]:" + user.getPoint(PointType.LEVEL) + " ("
                        + user.getPoint(PointType.EXP) + "\\" + user.getLevelCap() + ")" + //
                        "\n<command.total_exp>[Total EXP]: " + user.getTotalPoint(),
                false);

        builder.addField("<command.point>[Point]",
                "<command.total_money>[Total money]: " + user.getPoint(PointType.MONEY)
                        + "\n<command.total_pvp_point>[Total PvP Point]: "
                        + user.getPoint(PointType.PVP_POINT),
                false);

        builder.setColor(Color.BLUE);

        return builder;
    }

}
