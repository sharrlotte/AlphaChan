package AlphaChan.main.command.slash.subcommands.user;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import javax.annotation.Nonnull;

import AlphaChan.main.command.SlashSubcommand;
import AlphaChan.main.data.user.UserCache;
import AlphaChan.main.data.user.UserCache.PointType;
import AlphaChan.main.handler.UserHandler;

import java.util.List;
import java.awt.Color;

public class InfoCommand extends SlashSubcommand {
    public InfoCommand() {
        super("info", "Hiển thị thông tin người dùng", true, false);
        this.addOption(OptionType.USER, "user", "Tên thành viên", false);
    }

    @Override
    public String getHelpString() {
        return "Hiển thị thông tin người dùng:\n\t<user>: Tên người dùng muốn xem thông tin, nếu không nhập thì hiển thị thông tin bản thân";
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        OptionMapping userOption = event.getOption("user");

        // Display command caller information
        if (userOption == null) {
            Member member = event.getMember();
            if (member == null) {
                reply(event, "Lỗi không xác định", 10);
                return;
            }

            replyEmbed(event, getDisplayedUserInformation(member), 30);

        } else {
            // Display target information
            User user = userOption.getAsUser();
            Guild guild = event.getGuild();
            if (guild == null) {
                reply(event, "Lỗi không xác định", 10);
                return;
            }
            Member member = guild.getMember(user);
            if (member == null) {
                reply(event, "Người dùng với tên " + user.getName() + " không thuộc máy chủ", 10);
                return;
            }
            replyEmbed(event, getDisplayedUserInformation(member), 30);
        }
    }

    private EmbedBuilder getDisplayedUserInformation(@Nonnull Member member) {
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

        builder.addField("Vai trò", roleString, false);
        builder.addField("Thông tin cơ bản",
                "Cấp: " + user.getPoint(PointType.LEVEL) + " (" + user.getPoint(PointType.EXP) + "\\" + user.getLevelCap() + ")" + //
                        "\nTổng kinh nghiệm: " + user.getTotalPoint(),
                false);

        builder.addField("Điểm", "Tổng exp: " + user.getPoint(PointType.MONEY) + "\nTổng điểm pvp: " + user.getPoint(PointType.PVP_POINT),
                false);

        builder.setColor(Color.BLUE);

        return builder;
    }

}
