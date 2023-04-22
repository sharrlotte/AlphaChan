package alpha.main.command.slash.subcommands.user;

import alpha.main.command.SlashCommand;
import alpha.main.command.SlashSubcommand;
import alpha.main.data.user.UserCache;
import alpha.main.data.user.UserCache.PointType;
import alpha.main.handler.LocaleManager;
import alpha.main.handler.MessageHandler;
import alpha.main.handler.UserHandler;
import alpha.main.util.StringUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.HashMap;

public class TransferCommand extends SlashSubcommand {

    public TransferCommand() {
        super("transfer", "<command.command_transfer>[Transfer your point to others]", true, false);
        addCommandOption(OptionType.STRING, "type", "<command.point_type>[Point type]", true, true);
        addCommandOption(OptionType.USER, "user", "<command.user_name>[User you want to transfer to]", true);
        addCommandOption(OptionType.INTEGER, "point", "<command.amount>[Amount of point to transfer]", true);
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null)
            return;
        OptionMapping typeOption = event.getOption("type");
        if (typeOption == null)
            return;
        OptionMapping userOption = event.getOption("user");
        if (userOption == null)
            return;
        OptionMapping pointOption = event.getOption("point");
        if (pointOption == null)
            return;

        String result = "<command.invalid_point_type>[Error:Invalid point type]";

        try {

            User user = userOption.getAsUser();
            int point = pointOption.getAsInt();
            Member s = event.getMember();
            Member r = guild.getMember(user);

            if (r == null || s == null) {
                MessageHandler.replyTranslate(event.getHook(), "<command.invalid_user>[Error: User is not exists]", 30);
                return;
            }
            PointType type = PointType.valueOf(typeOption.getAsString());

            UserCache sender = UserHandler.getUserAwait(s);
            UserCache receiver = UserHandler.getUserNoCache(s);

            switch (type) {
                case LEVEL:
                case EXP:
                    break;

                default:
                    if (sender.getPoint(type) - point >= 0) {
                        sender.addPoint(type, -point);
                        receiver.addPoint(type, point);
                        result = LocaleManager
                                .format(guild, "<command.transfer>[Transferred %d %s to %s]", point, type.name(),
                                        receiver.getName());

                    } else {
                        result = LocaleManager.format(guild,
                                "<command.not_enough_point>[Not enough point to transfer, you only have %d %s]",
                                sender.getPoint(type), type.name());
                        break;
                    }

            }

        } finally {
            MessageHandler.reply(event.getHook(), StringUtils.backtick(result), 30);
        }

    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        String focus = event.getFocusedOption().getName();
        if (focus.equals("type")) {
            HashMap<String, String> options = new HashMap<String, String>();
            PointType[] type = PointType.values();

            for (int i = 2; i < type.length; i++)
                options.put(type[i].name(), type[i].name());

            SlashCommand.sendAutoComplete(event, options);
        }
    }
}
