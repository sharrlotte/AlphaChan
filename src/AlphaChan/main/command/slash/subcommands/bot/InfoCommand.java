package AlphaChan.main.command.slash.subcommands.bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import AlphaChan.BotConfig;
import AlphaChan.BotConfig.Config;
import AlphaChan.main.command.SlashSubcommand;
import AlphaChan.main.handler.GuildHandler;
import AlphaChan.main.handler.MessageHandler;
import AlphaChan.main.handler.UserHandler;

import static AlphaChan.AlphaChan.*;

import java.time.format.DateTimeFormatter;

public class InfoCommand extends SlashSubcommand {
    public InfoCommand() {
        super("info", "<@command.command_bot_info>");
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null)
            return;
        EmbedBuilder builder = new EmbedBuilder();
        StringBuilder field = new StringBuilder();
        Member yui = guild.getMemberById(BotConfig.readString(Config.YUI_ID, "11111111"));

        Long totalMember = 0l;
        User bot = jda.getSelfUser();

        for (Guild g : jda.getGuilds())
            totalMember += g.getMemberCount();

        if (yui == null)
            field.append("Chủ nhân: Yuilotte\n");
        else
            field.append("Chủ nhân: " + yui.getEffectiveName() + "\n");

        field.append("Máy chủ: " + GuildHandler.getActiveGuildCount() + "\\" + jda.getGuilds().size() + " trực tuyến\n");
        field.append("Thành viên: " + UserHandler.getActiveUserCount() + "\\" + totalMember + " hoạt động\n");
        field.append("Ngày sinh: " + bot.getTimeCreated().format(DateTimeFormatter.ofPattern("dd/MM/yyyy-hh:mm:ss")) + "\n");
        builder.addField("Thông tin", "```" + field.toString() + "```", false);
        builder.setThumbnail(bot.getEffectiveAvatarUrl());
        builder.setTitle(bot.getName(), jda.getInviteUrl(Permission.ADMINISTRATOR));

        MessageHandler.replyEmbed(event, builder, 30);
    }
}
