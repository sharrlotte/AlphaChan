package alpha.main.data.user;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import static alpha.main.AlphaChan.*;

import java.util.concurrent.ConcurrentHashMap;

import org.bson.Document;
import org.bson.conversions.Bson;

import alpha.main.BotConfig;
import alpha.main.BotConfig.Config;
import alpha.main.handler.DatabaseHandler;
import alpha.main.handler.GuildHandler;
import alpha.main.handler.DatabaseHandler.Database;
import alpha.main.handler.DatabaseHandler.LogCollection;
import alpha.main.util.Log;

public class UserCache extends TimeObject implements DatabaseObject {

    private final UserData data;

    public static enum PointType {
        EXP, LEVEL, MONEY, PVP_POINT;
    }

    public UserCache(String guildId, String userId) {
        this(new UserData());
        this.data.setUserId(userId);
        this.data.setGuildId(guildId);
    }

    public UserCache(UserData data) {
        super(BotConfig.readInt(Config.USER_ALIVE_TIME, 10));
        this.data = data;
    }

    public UserData getData() {
        return data;
    }

    public String getName() {
        if (!data.getName().isBlank())
            return data.getName();

        Member member = getMember();
        data.setName(member.getEffectiveName());

        return data.getName();
    }

    public String getHashId() {
        return data.getGuildId() + data.getUserId();
    }

    public int getLevelCap() {
        return getPoint(PointType.LEVEL) * getPoint(PointType.LEVEL);
    }

    public Integer getTotalPoint() {
        return ((getPoint(PointType.LEVEL) - 1) * getPoint(PointType.LEVEL) * (2 * (getPoint(PointType.LEVEL) - 1) + 1)
                / 6)
                + getPoint(PointType.EXP);
    }

    public Member getMember() {
        Guild guild = getGuild();
        Member member = guild.getMemberById(data.getUserId());
        if (member == null) {
            kill();
            throw new IllegalStateException(
                    "Member not found in guild with id + [" + data.getGuildId() + "] for user with id ["
                            + data.getUserId() + "]");
        }
        return member;
    }

    public Guild getGuild() {
        Guild guild = jda.getGuildById(data.getGuildId());
        if (guild == null) {
            kill();
            throw new IllegalStateException("Guild not found with id + [" + data.getGuildId() + "] for user with id ["
                    + data.getUserId() + "]");
        }
        return guild;
    }

    public int getPoint(PointType pointType, UserData data) {
        return data.getPoints().get(pointType.ordinal());
    }

    public int getPoint(PointType pointType) {
        return getPoint(pointType, data);
    }

    public void setPoint(PointType pointType, int value, UserData data) {
        data.getPoints().set(pointType.ordinal(), value);
    }

    public void setPoint(PointType pointType, int value) {
        setPoint(pointType, value, data);
    }

    public int addPoint(PointType pointType, int value, UserData data) {
        data.getPoints().set(pointType.ordinal(), value + getPoint(pointType, data));
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
            throw new IllegalStateException("Member is not exists");

        // If bot has a higher role position
        if (!bot.canInteract(member))
            return;

        for (String key : levelRoleId.keySet()) {
            if (levelRoleId.get(key) == getPoint(PointType.LEVEL)) {
                if (key == null)
                    return;
                Role role = guild.getRoleById(key);
                if (role == null)
                    return;

                guild.addRoleToMember(member, role).queue();
                Log.info("AUTO-ROLE", "Đã thêm vai trò " + role.getName() + " cho " + member.getEffectiveName());
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
    @Override
    public void update(Runnable cacheCleaner) {
        Bson filter = new Document().append("userId", data.getUserId());
        DatabaseHandler.updateAndFinish(Database.USER, data.getGuildId(), UserData.class, filter, data,
                cacheCleaner);
    }

    @Override
    public void delete() {
        Bson filter = new Document().append("userId", data.getGuildId());
        DatabaseHandler.delete(Database.USER, data.getGuildId(), UserData.class, filter);
        DatabaseHandler.log(LogCollection.DATABASE, "DELETE USER", data.toDocument().toString());
    }
}
