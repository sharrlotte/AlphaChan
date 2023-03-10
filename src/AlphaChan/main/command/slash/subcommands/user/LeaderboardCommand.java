package AlphaChan.main.command.slash.subcommands.user;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.HashMap;

import AlphaChan.main.command.SimpleBotSubcommand;
import AlphaChan.main.gui.discord.table.LeaderboardTable;

public class LeaderboardCommand extends SimpleBotSubcommand {

    public enum ORDER {
        LEVEL, MONEY, PVP_POINT
    }

    public enum LEADERBOARD {
        GUILD, ONLINE, ALL
    }

    public LeaderboardCommand() {
        super("leaderboard", "Hiện thị bản xếp hạng của người dùng", true, false);
        this.addOptions(new OptionData(OptionType.STRING, "orderby", "Tên bảng xếp hạng", true, true)).//
                addOptions(new OptionData(OptionType.STRING, "leaderboard", "Tên bảng xếp hạng", true, true));
    }

    @Override
    public String getHelpString() {
        return "Hiện thị bản xếp hạng của người dùng:\n\t<orderby>: Xếp theo:\n\t\t- MONEY: Xếp theo tiền\n\t\t- LEVEL: Xếp theo cấp\n\t\t- PVP_POINT: Xếp theo điểm pvp";
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (member == null)
            throw new IllegalStateException("User not in guild");
        OptionMapping orderOption = event.getOption("orderby");
        OptionMapping leaderboardOption = event.getOption("leaderboard");
        ORDER order;
        LEADERBOARD leaderboard;

        // Default is sort by level
        if (orderOption == null)
            order = ORDER.LEVEL;
        else
            order = ORDER.valueOf(orderOption.getAsString());

        if (leaderboardOption == null)
            leaderboard = LEADERBOARD.GUILD;
        else
            leaderboard = LEADERBOARD.valueOf(leaderboardOption.getAsString());

        LeaderboardTable table = new LeaderboardTable(event, leaderboard, order);
        table.sendTable();
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        String focus = event.getFocusedOption().getName();
        if (focus.equals("orderby")) {
            HashMap<String, String> options = new HashMap<String, String>();
            for (ORDER t : ORDER.values())
                options.put(t.name(), t.name());
            sendAutoComplete(event, options);

        } else if (focus.equals("leaderboard")) {
            HashMap<String, String> options = new HashMap<String, String>();
            for (LEADERBOARD t : LEADERBOARD.values())
                options.put(t.name(), t.name());
            sendAutoComplete(event, options);
        }
    }
}
