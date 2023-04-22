package alpha.main.command.slash.subcommands.user;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.TimeFormat;
import alpha.main.command.SlashSubcommand;
import alpha.main.data.user.UserCache;
import alpha.main.data.user.UserCache.PointType;
import alpha.main.handler.DatabaseHandler;
import alpha.main.handler.LocaleManager;
import alpha.main.handler.MessageHandler;
import alpha.main.handler.UserHandler;
import alpha.main.handler.DatabaseHandler.Database;
import alpha.main.util.StringUtils;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;

public class DailyCommand extends SlashSubcommand {
    public DailyCommand() {
        super("daily", "<command.command_daily>[Get daily reward]", true, false);
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        Guild guild = event.getGuild();
        if (guild == null)
            throw new IllegalStateException("MEMBER IS NOT EXISTS");

        if (member == null)
            throw new IllegalStateException("MEMBER IS NOT EXISTS");

        if (!DatabaseHandler.collectionExists(Database.DAILY, guild.getId()))
            DatabaseHandler.createCollection(Database.DAILY, guild.getId());

        MongoCollection<Document> collection = DatabaseHandler.getDatabase(Database.DAILY).getCollection(guild.getId());

        Bson filter = new Document().append("userId", member.getId());
        Document data = collection.find(filter).limit(1).first();
        UserCache userData = UserHandler.getUserAwait(member);

        int money = 0;
        Document result = new Document().append("userId", userData.getData().getUserId()).append("time",
                System.currentTimeMillis());

        if (data == null || data.isEmpty()) {
            money = userData.addPoint(PointType.MONEY, userData.getLevelCap());
            collection.insertOne(result);

        } else {
            if (data.containsKey("time")) {
                Long time = (Long) data.get("time");
                if (System.currentTimeMillis() - time >= 86400000l) { // 1 Day
                    money = userData.addPoint(PointType.MONEY, userData.getLevelCap());
                    collection.replaceOne(filter, result);
                }
            }
        }

        if (money > 0)
            MessageHandler.reply(event.getHook(),
                    LocaleManager.format(event.getGuild(),
                            StringUtils.backtick(
                                    "<command.daily_success>[Reward %d Alpha \nCurrent point: %d ]"),
                            money, userData.getPoint(PointType.MONEY)),
                    30);
        else {
            if (data != null) {
                if (data.containsKey("time")) {
                    Long lastTime = (Long) data.get("time");
                    MessageHandler.reply(event.getHook(), LocaleManager.format(event.getGuild(),
                            StringUtils.backtick("<command.daily_fail>[Cooldown until %s \nLast time: %s]"),
                            TimeFormat.RELATIVE.atTimestamp(lastTime).plus(24 * 60 * 60 * 1000),
                            TimeFormat.DATE_TIME_SHORT.format(lastTime)), 30);
                }
            }
        }
    }
}
