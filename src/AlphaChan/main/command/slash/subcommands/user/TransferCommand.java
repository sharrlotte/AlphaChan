package AlphaChan.main.command.slash.subcommands.user;

import AlphaChan.main.command.SlashCommand;
import AlphaChan.main.command.SlashSubcommand;
import AlphaChan.main.data.user.UserCache;
import AlphaChan.main.data.user.UserCache.PointType;
import AlphaChan.main.handler.MessageHandler;
import AlphaChan.main.handler.UserHandler;
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
        super("transfer", "<?command.command_transfer>", true, false);
        addOption(OptionType.STRING, "type", "<?command.point_type>", true, true);
        addOption(OptionType.USER, "user", "<?command.user_name>", true);
        addOption(OptionType.INTEGER, "point", "<?command.amount>", true);
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

        try {

            User user = userOption.getAsUser();
            int point = pointOption.getAsInt();
            Member s = event.getMember();
            Member r = guild.getMember(user);

            if (r == null || s == null) {
                MessageHandler.reply(event, "<?command.invalid_user>", 30);
                return;
            }
            PointType type = PointType.valueOf(typeOption.getAsString());

            UserCache sender = UserHandler.getUserAwait(s);
            UserCache receiver = UserHandler.getUserNoCache(s);
            String result = "<?command.invalid_point_type>";

            switch (type) {
            case LEVEL:
            case EXP:
                break;

            default:
                if (sender.getPoint(type) - point >= 0) {
                    sender.addPoint(type, -point);
                    receiver.addPoint(type, point);
                    result = "<?command.transfer> " + point + " <?command.pvp_point> " + receiver.getName();

                } else {
                    result = "<?command.not_enough>";
                    break;
                }

            }

            MessageHandler.reply(event, result, 30);
        } catch (Exception e) {
            MessageHandler.reply(event, "<?command.invalid_point_type>", 10);
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
