package AlphaChan.main.command.subcommands.YuiCommands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.Arrays;
import java.util.HashMap;

import AlphaChan.main.handler.UserHandler;
import AlphaChan.main.user.UserData;
import AlphaChan.main.util.SimpleBotSubcommand;

public class AddCommand extends SimpleBotSubcommand {

    private enum POINT_TYPE {
        MONEY, PVP
    }

    public AddCommand() {
        super("add", "Yui only");
        this.addOption(OptionType.STRING, "type", "Yui only", true, true).//
                addOption(OptionType.USER, "user", "Yui only", true).//
                addOption(OptionType.INTEGER, "point", "Yui only", true);
    }

    @Override
    public String getHelpString() {
        return "";
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

        String type = typeOption.getAsString();
        User user = userOption.getAsUser();
        int point = pointOption.getAsInt();
        Member r = guild.getMember(user);
        Member s = event.getMember();

        if (r == null || s == null)
            return;

        UserData sender = UserHandler.getUserNoCache(s);
        UserData receiver = UserHandler.getUserNoCache(r);

        POINT_TYPE pt = POINT_TYPE.valueOf(type);

        switch (pt) {
            case MONEY:
                if (sender.money >= point) {
                    sender.money -= point;
                    receiver.money += point;
                }
                break;
            case PVP:
                if (sender.pvpPoint >= point) {
                    sender.pvpPoint -= point;
                    receiver.pvpPoint += point;
                }
                break;
            default:
                reply(event, "Chuyển không thành công, giá trị " + type + " không hợp lệ", 30);
                return;
        }
        reply(event, "Chuyển thành công " + point + " " + type + " đến " + r.getEffectiveName(), 30);
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        String focus = event.getFocusedOption().getName();
        if (focus.equals("type")) {
            HashMap<String, String> options = new HashMap<String, String>();
            Arrays.asList(POINT_TYPE.values()).forEach(t -> options.put(t.name(), t.name()));
            sendAutoComplete(event, options);
        }
    }

}
