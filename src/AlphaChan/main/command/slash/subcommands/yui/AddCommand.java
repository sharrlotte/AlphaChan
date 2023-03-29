package AlphaChan.main.command.slash.subcommands.yui;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.HashMap;

import AlphaChan.main.command.SlashCommand;
import AlphaChan.main.command.SlashSubcommand;
import AlphaChan.main.data.user.UserCache;
import AlphaChan.main.data.user.UserCache.PointType;
import AlphaChan.main.handler.MessageHandler;
import AlphaChan.main.handler.UserHandler;

public class AddCommand extends SlashSubcommand {

    public AddCommand() {
        super("add", "Yui only");
        addOption(OptionType.STRING, "type", "Yui only", true, true);
        addOption(OptionType.USER, "user", "Yui only", true);
        addOption(OptionType.INTEGER, "point", "Yui only", true);
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        OptionMapping typeOption = event.getOption("type");
        if (typeOption == null)
            return;
        OptionMapping userOption = event.getOption("user");
        if (userOption == null)
            return;
        OptionMapping pointOption = event.getOption("point");
        if (pointOption == null)
            return;

        Guild guild = event.getGuild();
        if (guild == null)
            return;

        User user = userOption.getAsUser();
        int point = pointOption.getAsInt();
        Member r = guild.getMember(user);

        if (r == null)
            return;

        try {
            PointType type = PointType.valueOf(typeOption.getAsString());

            UserCache receiver = UserHandler.getUserNoCache(r);

            String result = "<?command.invalid_point_type>";

            switch (type) {
            case LEVEL:
            case EXP:
                break;

            default:
                receiver.addPoint(type, point);
                result = "<?command.add> " + point + " <?command.point> " + type.name() + " <?command.to> " + receiver.getName();
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
