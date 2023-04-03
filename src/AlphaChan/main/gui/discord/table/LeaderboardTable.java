package AlphaChan.main.gui.discord.table;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import AlphaChan.main.command.slash.subcommands.user.LeaderboardCommand.LEADERBOARD;
import AlphaChan.main.command.slash.subcommands.user.LeaderboardCommand.ORDER;
import AlphaChan.main.data.user.UserCache;
import AlphaChan.main.data.user.UserData;
import AlphaChan.main.data.user.UserCache.PointType;
import AlphaChan.main.gui.discord.PageTable;
import AlphaChan.main.handler.DatabaseHandler;
import AlphaChan.main.handler.UserHandler;
import AlphaChan.main.handler.DatabaseHandler.Database;
import AlphaChan.main.util.Log;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import static AlphaChan.AlphaChan.*;

public class LeaderboardTable extends PageTable {

    private final LEADERBOARD leaderboard;
    private final ORDER order;
    private final int MAX_DISPLAY = 10;

    private List<UserCache> users = new ArrayList<UserCache>();

    public LeaderboardTable(SlashCommandInteractionEvent event, LEADERBOARD leaderboard, ORDER order) {
        super(event, 2);
        this.leaderboard = leaderboard;
        this.order = order;

        addButton(button("<<<", ButtonStyle.PRIMARY, () -> firstPage()));
        addButton(button("<", ButtonStyle.PRIMARY, () -> previousPage()));
        addButton(button("X", ButtonStyle.PRIMARY, () -> deleteTable()));
        addButton(button(">", ButtonStyle.DANGER, () -> nextPage()));
        addButton(button(">>>", ButtonStyle.PRIMARY, () -> lastPage()));
    }

    @Override
    public EmbedBuilder getCurrentPage() {
        if (users.size() <= 0)
            getLeaderboardData(this.leaderboard, this.order);

        return addPageFooter(getLeaderboard());
    }

    @Override
    public void lastPage() {
        this.pageNumber = getMaxPage() - 1;
        updateTable();
    }

    @Override
    public int getMaxPage() {
        return (Integer) ((users.size() - 1) / MAX_DISPLAY) + 1;
    }

    public void getLeaderboardData(LEADERBOARD leaderboard, ORDER order) {

        switch (leaderboard) {
            case ALL:
                jda.getGuilds().forEach(guild -> {
                    String guildId = guild.getId();
                    MongoCollection<UserData> collection = DatabaseHandler.getCollection(Database.USER, guildId,
                            UserData.class);
                    try {
                        FindIterable<UserData> data = collection.find();
                        data.forEach(d -> users.add(new UserCache(d)));
                    } catch (Exception e) {

                    }
                });
                break;

            case GUILD:
                Guild guild = getEvent().getGuild();
                if (guild == null)
                    throw new IllegalStateException("Guild not found");

                String guildId = guild.getId();
                MongoCollection<UserData> collection = DatabaseHandler.getCollection(Database.USER, guildId,
                        UserData.class);

                FindIterable<UserData> data = collection.find();
                data.forEach(d -> users.add(new UserCache(d)));
                break;

            case ONLINE:
                users.addAll(UserHandler.getCachedUser());
        }

        switch (order) {
            case MONEY:
                users.sort(new Comparator<UserCache>() {
                    @Override
                    public int compare(UserCache a, UserCache b) {
                        return b.getPoint(PointType.MONEY) - a.getPoint(PointType.MONEY);
                    }
                });
                break;

            case PVP_POINT:
                users.sort(new Comparator<UserCache>() {
                    @Override
                    public int compare(UserCache a, UserCache b) {
                        return b.getPoint(PointType.PVP_POINT) - a.getPoint(PointType.PVP_POINT);
                    }
                });
                break;

            default:
                users.sort(new Comparator<UserCache>() {
                    @Override
                    public int compare(UserCache a, UserCache b) {
                        return b.getTotalPoint() - a.getTotalPoint();
                    }
                });
                break;
        }
    }

    public EmbedBuilder getLeaderboard() {

        Member member = getEventMember();
        if (member == null)
            return new EmbedBuilder().addField("NULL", "<?command.user_not_found>", false);

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("<?command.leaderboard> (" + leaderboard + ")");

        UserCache user = UserHandler.getUserNoCache(member);
        int position = users.indexOf(user);

        int length = Math.min((pageNumber + 1) * MAX_DISPLAY, users.size());
        int start = pageNumber * MAX_DISPLAY;
        for (int i = start; i < length; i++)
            builder.addField("Hạng: " + (i + 1), getUserInformation(users.get(i), order), false);

        // Display sender position if its not contained in the leaderboard
        if (position <= pageNumber * MAX_DISPLAY || position > (pageNumber + 1) * MAX_DISPLAY)
            if (position >= 0)
                builder.addField("Hạng: " + (position + 1), getUserInformation(user, order), false);

        return builder;
    }

    public String getUserInformation(UserCache user, ORDER order) {
        try {
            String data = user.getName() + ": ";
            switch (order) {
                case MONEY:
                    data += user.getPoint(PointType.MONEY) + " Alpha";
                    break;

                case PVP_POINT:
                    data += user.getPoint(PointType.PVP_POINT) + " <?command.point>";
                    break;

                default:
                    data += "<?command.level> " + user.getPoint(PointType.LEVEL) + " (" + user.getPoint(PointType.EXP)
                            + " <?command.exp>)";
                    break;
            }
            data += "\n<?command.guild>: " + user.getGuild().getName();
            return data;

        } catch (Exception e) {
            Log.error(e);
            return "<?command.user_left_guild>";
        }
    }
}
