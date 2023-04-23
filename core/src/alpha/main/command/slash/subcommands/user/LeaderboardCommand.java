package alpha.main.command.slash.subcommands.user;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.HashMap;

import alpha.main.command.SlashCommand;
import alpha.main.command.SlashSubcommand;
import alpha.main.ui.discord.table.LeaderboardTable;

public class LeaderboardCommand extends SlashSubcommand {

    public enum ORDER {
        LEVEL, MONEY, PVP_POINT
    }

    public enum LEADERBOARD {
        GUILD, ONLINE, ALL
    }

    public LeaderboardCommand() {
        super("leaderboard", "<command.command_leaderboard>[Show the leaderboard]", true, false);
        addCommandOption(OptionType.STRING, "orderby", "<command.order>[Order]", true, true);
        addCommandOption(OptionType.STRING, "leaderboard", "<command.name>[Scope]", true, true);
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
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
