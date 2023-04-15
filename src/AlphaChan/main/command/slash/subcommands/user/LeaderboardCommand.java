package AlphaChan.main.command.slash.subcommands.user;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.HashMap;

import AlphaChan.main.command.SlashCommand;
import AlphaChan.main.command.SlashSubcommand;
import AlphaChan.main.ui.discord.table.LeaderboardTable;

public class LeaderboardCommand extends SlashSubcommand {

    public enum ORDER {
        LEVEL, MONEY, PVP_POINT
    }

    public enum LEADERBOARD {
        GUILD, ONLINE, ALL
    }

    public LeaderboardCommand() {
        super("leaderboard", "<?command.command_leaderboard>", true, false);
        addOptions(new OptionData(OptionType.STRING, "orderby", "<?command.order>", true, true));
        addOptions(new OptionData(OptionType.STRING, "leaderboard", "<?command.name>", true, true));
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
            SlashCommand.sendAutoComplete(event, options);

        } else if (focus.equals("leaderboard")) {
            HashMap<String, String> options = new HashMap<String, String>();
            for (LEADERBOARD t : LEADERBOARD.values())
                options.put(t.name(), t.name());
            SlashCommand.sendAutoComplete(event, options);
        }
    }
}
