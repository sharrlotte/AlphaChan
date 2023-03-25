package AlphaChan.main.command.slash.subcommands.user;

import AlphaChan.main.command.SimpleBotSubcommand;
import AlphaChan.main.data.user.UserCache;
import AlphaChan.main.data.user.UserCache.PointType;
import AlphaChan.main.handler.UserHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.HashMap;

public class TransferCommand extends SimpleBotSubcommand {

    public TransferCommand() {
        super("transfer", "Chuyển chỉ số cho người khác", true, false);
        addOption(OptionType.STRING, "type", "Loại chỉ số muốn chuyển", true, true);
        addOption(OptionType.USER, "user", "Người muốn chuyển", true);
        addOption(OptionType.INTEGER, "point", "Số điểm muốn chuyển", true);
    }

    @Override
    public String getHelpString() {
        return "Chuyển chỉ số cho người khác:\n\t<type>: Loại chỉ số muốn chuyển:\n\t\t- MONEY: chuyển tiền\n\t\t- PVPPoint: chuyển điểm pvp\n\t<user>: Tên người muốn chuyển cho\n\t<point>: số điểm muốn chuyển";
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
                reply(event, "Người nhận không hợp lệ", 30);
                return;
            }
            PointType type = PointType.valueOf(typeOption.getAsString());

            UserCache sender = UserHandler.getUserAwait(s);
            UserCache receiver = UserHandler.getUserNoCache(s);
            String result = "Loại điểm muốn chuyển không hợp lệ";

            switch (type) {
            case LEVEL:
            case EXP:
                break;

            default:
                if (sender.getPoint(type) - point >= 0) {
                    sender.addPoint(type, -point);
                    receiver.addPoint(type, point);
                    result = "Đã chuyển " + point + " điểm PVP cho " + receiver.getData().getName();

                } else {
                    result = "Không đủ điểm để chuyển";
                    break;
                }

            }

            reply(event, result, 30);
        } catch (Exception e) {
            reply(event, "Loại điểm muốn chuyển không hợp lệ", 10);
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

            sendAutoComplete(event, options);
        }
    }
}
