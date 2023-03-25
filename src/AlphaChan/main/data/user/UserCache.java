package AlphaChan.main.data.user;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.concurrent.ConcurrentHashMap;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;

import AlphaChan.main.handler.DatabaseHandler;
import AlphaChan.main.handler.GuildHandler;
import AlphaChan.main.handler.DatabaseHandler.Database;
import AlphaChan.main.handler.DatabaseHandler.LogType;

import static AlphaChan.AlphaChan.jda;

public class UserCache extends TimeObject implements DatabaseObject {

    private final UserData data;

    public static enum PointType {
        EXP, LEVEL, MONEY, PVP_POINT;
    }

    public UserCache(String userId, String guildId) {
        this.data = new UserData();
        this.data.setUserId(userId);
        this.data.setGuildId(guildId);
    }

    public UserCache(UserData data) {
        this.data = data;
    }

    public UserData getData() {
        return data;
    }

    public String getHashId() {
        return data.getGuildId() + data.getUserId();
    }

    public int getLevelCap() {
        return getPoint(PointType.LEVEL) * getPoint(PointType.LEVEL);
    }

    public Integer getTotalPoint() {
        return ((getPoint(PointType.LEVEL) - 1) * getPoint(PointType.LEVEL) * (2 * (getPoint(PointType.LEVEL) - 1) + 1)
                / 6) + getPoint(PointType.EXP);
    }

    public Member getMember() {
        Guild guild = getGuild();
        Member member = guild.getMemberById(data.getUserId());
        if (member == null) {
            killTimer();
            throw new IllegalStateException("MEMBER IS NOT EXISTS");
        }
        return member;
    }

    public Guild getGuild() {
        Guild guild = jda.getGuildById(data.getGuildId());
        if (guild == null) {
            killTimer();
            throw new IllegalStateException("GUILD IS NOT EXISTS");
        }
        return guild;
    }

    public int getPoint(PointType pointType, UserData data) {
        return data.getPoints()[pointType.ordinal()];
    }

    public int getPoint(PointType pointType) {
        return getPoint(pointType, data);
    }

    public void setPoint(PointType pointType, int value, UserData data) {
        data.getPoints()[pointType.ordinal()] = value;
    }

    public void setPoint(PointType pointType, int value) {
        setPoint(pointType, value, data);
    }

    public int addPoint(PointType pointType, int value, UserData data) {
        data.getPoints()[pointType.ordinal()] += value;
        return value;
    }

    public int addPoint(PointType pointType, int value) {
        return addPoint(pointType, value, data);
    }

    // Add point for user
    public boolean addExp(int point) {
        boolean levelUp = false;
        int extra;

        while (getPoint(PointType.EXP) + point >= getLevelCap()) {
            extra = getLevelCap() - getPoint(PointType.EXP);
            point -= extra;
            setPoint(PointType.EXP, 0);
            addPoint(PointType.LEVEL, 1);
            levelUp = true;
            checkLevelRole();
        }
        addPoint(PointType.EXP, point);
        return levelUp;
    }

    // Add role to member when data.getLevel() is satisfied
    public void checkLevelRole() {
        ConcurrentHashMap<String, Integer> levelRoleId = GuildHandler.getGuild(data.getGuildId()).getData()
                .getLevelRoleId();

        Guild guild = getGuild();
        Member bot = guild.getSelfMember();
        Member member = getMember();
        if (bot == null || member == null)
            return;

        // If bot has a higher role position
        if (!bot.canInteract(member))
            return;

        for (String key : levelRoleId.keySet()) {
            if (levelRoleId.get(key) <= getPoint(PointType.LEVEL)) {
                if (key == null)
                    return;
                Role role = guild.getRoleById(key);
                if (role == null)
                    return;
                guild.addRoleToMember(member, role);
            }
        }
    }

    public UserCache merge(UserCache cache) {
        if (cache == null || cache.getData() == null)
            return this;

        PointType[] type = PointType.values();

        for (int i = 1; i < type.length; i++)
            addPoint(type[i], getPoint(type[i], cache.getData()));

        return this;
    }

    // Update user on Database
    public void update() {
        // If this user is deleted, don't save
        if (isAlive())
            return;

        MongoCollection<UserData> collection = DatabaseHandler.getCollection(Database.USER, data.getGuildId(),
                UserData.class);

        // Filter for user id, user id is unique for each collection
        Bson filter = new Document().append("userId", data.getUserId());
        collection.replaceOne(filter, data, new ReplaceOptions().upsert(true));
    }

    public void delete() {
        MongoCollection<UserData> collection = DatabaseHandler.getCollection(Database.USER, data.getGuildId(),
                UserData.class);
        // Filter for user id, user id is unique for each collection
        Bson filter = new Document().append("userId", data.getGuildId());
        collection.deleteOne(filter);
        DatabaseHandler.log(LogType.Database, new Document().append("DELETE USER", data.toDocument()));
        killTimer();
    }
}
