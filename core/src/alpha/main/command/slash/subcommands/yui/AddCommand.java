package alpha.main.command.slash.subcommands.yui;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.HashMap;

import alpha.main.command.SlashCommand;
import alpha.main.command.SlashSubcommand;
import alpha.main.data.user.UserCache;
import alpha.main.data.user.UserCache.PointType;
import alpha.main.handler.LocaleManager;
import alpha.main.handler.MessageHandler;
import alpha.main.handler.UserHandler;
import alpha.main.util.StringUtils;

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

        String result = "<command.invalid_point_type>[Error:Invalid point type]";

        try {
            PointType type = PointType.valueOf(typeOption.getAsString());

            UserCache receiver = UserHandler.getUserNoCache(r);

            switch (type) {
                case LEVEL:
                case EXP:
                    break;

                default:
                    receiver.addPoint(type, point);
                    result = LocaleManager.format(event.getGuild(), "<command.point_added> [Added %d %s to %s ]",
                            point,
                            type.name(),
                            receiver.getName());
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
