package alpha.main.command.slash.subcommands.bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import alpha.main.BotConfig;
import alpha.main.BotConfig.Config;
import alpha.main.command.SlashSubcommand;
import alpha.main.handler.GuildHandler;
import alpha.main.handler.MessageHandler;
import alpha.main.handler.UserHandler;

import static alpha.main.AlphaChan.*;

import java.time.format.DateTimeFormatter;

public class InfoCommand extends SlashSubcommand {
    public InfoCommand() {
        super("info", "<command.command_bot_info>[Show bot information]");
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

        field.append("<command.guild_owner>[Guild owner]: " + (yui == null ? "Yui" : yui.getEffectiveName()) + "\n");

        field.append(
                "<command.guild_active>[Guild active]: " + GuildHandler.getActiveGuildCount() + "/"
                        + jda.getGuilds().size() + "\n");
        field.append("<command.member_active>[Member active]: " + UserHandler.getActiveUserCount() + "/" + totalMember
                + "\n");
        field.append(
                "<command.guild_create_time>[Guild create time]: "
                        + bot.getTimeCreated().format(DateTimeFormatter.ofPattern("dd/MM/yyyy-hh:mm:ss")) + "\n");
        builder.addField("Info", "```" + field.toString() + "```", false);
        builder.setThumbnail(bot.getEffectiveAvatarUrl());
        builder.setTitle(bot.getName(), jda.getInviteUrl(Permission.ADMINISTRATOR));

        MessageHandler.replyEmbedTranslate(event.getHook(), builder, 30);
    }
}
