package AlphaChan.main.command.slash.subcommands.user;

import AlphaChan.main.command.SimpleBotSubcommand;
import AlphaChan.main.data.user.UserData;
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

    enum TYPE {
        PVP_POINT, MONEY
    }

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

        String type = typeOption.getAsString();
        User user = userOption.getAsUser();
        int point = pointOption.getAsInt();
        Member sender = event.getMember();
        Member receiver = guild.getMember(user);

        if (receiver == null || sender == null) {
            reply(event, "Người nhận không hợp lệ", 30);
            return;
        }
        UserData senderData = UserHandler.getUserAwait(sender);
        UserData receiverData = UserHandler.getUserNoCache(receiver);
        String result;

        switch (TYPE.valueOf(type)) {
            case MONEY:
                if (senderData.money - point >= 0) {
                    senderData._addMoney(-point);
                    receiverData._addMoney(point);
                    result = "Đã chuyển " + point + " điểm Alpha cho " + receiverData._getName();
                } else
                    result = "Không đủ điểm để chuyển";
                break;
            case PVP_POINT:
                if (senderData.pvpPoint - point >= 0) {
                    senderData._addPVPPoint(point);
                    receiverData._addPVPPoint(point);
                    result = "Đã chuyển " + point + " điểm PVP cho " + receiverData._getName();
                } else
                    result = "Không đủ điểm để chuyển";
                break;
            default:
                result = "Giá trị <type> không hợp lệ: ";
        }
        reply(event, result, 30);

    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        String focus = event.getFocusedOption().getName();
        if (focus.equals("type")) {
            HashMap<String, String> options = new HashMap<String, String>();
            for (TYPE t : TYPE.values())
                options.put(t.name(), t.name());
            sendAutoComplete(event, options);
        }
    }
}
