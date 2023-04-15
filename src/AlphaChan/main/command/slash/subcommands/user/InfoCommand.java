package AlphaChan.main.command.slash.subcommands.user;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import AlphaChan.main.command.SlashSubcommand;
import AlphaChan.main.data.user.UserCache;
import AlphaChan.main.data.user.UserCache.PointType;
import AlphaChan.main.handler.MessageHandler;
import AlphaChan.main.handler.UserHandler;

import java.util.List;
import java.awt.Color;

public class InfoCommand extends SlashSubcommand {
    public InfoCommand() {
        super("info", "<?command.command_user_info>", true, false);
        this.addOption(OptionType.USER, "user", "<?command.user_name>", false);
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        OptionMapping userOption = event.getOption("user");

        // Display command caller information
        if (userOption == null) {
            Member member = event.getMember();
            if (member == null) {
                return;
            }

            MessageHandler.replyEmbed(event, getDisplayedUserInformation(member), 30);

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
            MessageHandler.replyEmbed(event, getDisplayedUserInformation(member), 30);
        }
    }

    private EmbedBuilder getDisplayedUserInformation(Member member) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(member.getEffectiveName(), null, member.getEffectiveAvatarUrl());
        builder.setThumbnail(member.getEffectiveAvatarUrl());
        // Display role
        List<Role> roles = member.getRoles();
        String roleString = "";
        for (Role role : roles)
            roleString += role.getAsMention() + ", ";

        if (!roleString.isEmpty())
            roleString = roleString.substring(0, roleString.length() - 2);
        // Display point

        UserCache user = UserHandler.getUserNoCache(member);

        builder.addField("<?command.role>", roleString, false);
        builder.addField("<?command.basic_info>", "<?command.level>: " + user.getPoint(PointType.LEVEL) + " ("
                + user.getPoint(PointType.EXP) + "\\" + user.getLevelCap() + ")" + //
                "\n<?command.total_exp>: " + user.getTotalPoint(), false);

        builder.addField("<?command.point>",
                "<?command.total_money>: " + user.getPoint(PointType.MONEY) + "\n<?command.total_pvp_point>: "
                        + user.getPoint(PointType.PVP_POINT),
                false);

        builder.setColor(Color.BLUE);

        return builder;
    }

}
