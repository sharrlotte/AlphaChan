package AlphaChan.main.handler;

import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import AlphaChan.BotConfig;
import AlphaChan.BotConfig.Config;
import AlphaChan.main.data.user.GuildCache;
import AlphaChan.main.data.user.UserCache;
import AlphaChan.main.data.user.UserCache.PointType;
import AlphaChan.main.handler.DatabaseHandler.Database;
import AlphaChan.main.handler.DatabaseHandler.LogType;
import AlphaChan.main.util.Log;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;

public final class UserHandler implements Updatable {

    private static UserHandler instance = new UserHandler();
    // Hash map to store user cache
    private static ConcurrentHashMap<String, UserCache> userCache = new ConcurrentHashMap<>();

    private UserHandler() {
        UpdatableHandler.addListener(this);

        Log.system("User handler up");
    }

    public static UserHandler getInstance() {
        if (instance == null)
            instance = new UserHandler();
        return instance;
    }

    public static Collection<UserCache> getUserCache() {
        return userCache.values();
    }

    public void update() {
        updateCache();
    }

    public void updateCache() {
        Iterator<UserCache> iterator = userCache.values().iterator();
        while (iterator.hasNext()) {
            UserCache user = iterator.next();
            if (!user.isAlive(1)) {
                Log.info("STATUS", "User <" + user.getData().getName() + ":" + user.getData().getUserId() + "> offline");
                UpdatableHandler.updateStatus();
                user.update();
                iterator.remove();
            }
        }
    }

    public static int getActiveUserCount() {
        return userCache.size();
    }

    public static Collection<UserCache> getCachedUser() {
        return userCache.values();
    }

    // Get date for daily command
    public static String getDate() {
        return (Calendar.getInstance().getTime()).toString();
    }

    // Update point, money, level on massage sent
    public static void onMessage(Message message) {
        Member member = message.getMember();
        if (member == null) {
            throw new IllegalStateException("MEMBER IS NOT EXISTS");
        }
        UserCache user = getUserAwait(member);

        user.resetTimer();

        user.addPoint(PointType.MONEY, 1);
        user.addExp(1);
    }

    public static boolean isYui(Member member) {
        if (member == null)
            return false;
        return (member.getId().equals(BotConfig.readString(Config.YUI_ID, null)));
    }

    public static boolean isAdmin(Member member) {
        if (member == null)
            return false;

        if (isYui(member))
            return true;

        if (member.isOwner())
            return true;

        List<Role> roles = member.getRoles();

        GuildCache guildData = GuildHandler.getGuild(member.getGuild().getId());
        for (String adminId : guildData.getData().getAdminRoleId()) {
            for (Role role : roles) {
                if (role.getId().equals(adminId))
                    return true;
            }
        }
        return false;
    }

    // Add user to cache
    public static UserCache addUser(@Nonnull String guildId, String userId) {
        UserCache userData = new UserCache(guildId, userId);
        // Key is hashId = guildId + userId
        userCache.put(userData.getHashId(), userData);
        Log.info("STATUS", "User <" + userData.getData().getName() + ":" + userId + "> online");
        UpdatableHandler.updateStatus();
        return userData;
    }

    // Add user to cache
    public static UserCache addUser(Member member) {
        return addUser(member.getGuild().getId(), member.getId());
    }

    public static UserCache getUserNoCache(@Nonnull String guildId, String userId) {
        String hashId = guildId + userId;
        if (userCache.containsKey(hashId))
            return userCache.get(hashId);
        return getUserFromDatabase(guildId, userId);
    }

    // Get user without adding it to cache
    public static UserCache getUserNoCache(@Nonnull Member member) {
        String guildId = member.getGuild().getId();
        String userId = member.getId();
        // If user exist in cache then return, else query user from Database
        return getUserNoCache(guildId, userId);
    }

    // Waiting for data from Database
    public static UserCache getUserAwait(@Nonnull Member member) {
        String guildId = member.getGuild().getId();
        String userId = member.getId();
        // If user exist in cache then return, else query user from Database
        String hashId = guildId + userId;
        if (userCache.containsKey(hashId))
            return userCache.get(hashId);

        UserCache userFromCache = addUser(guildId, userId);
        UserCache userFromDatabase = getUserFromDatabase(guildId, userId);
        userFromDatabase.merge(userFromCache);
        userCache.put(hashId, userFromDatabase);
        return userFromDatabase;
    }

    public static ConcurrentHashMap<String, UserCache> getUserFromGuild(@Nonnull String guildId) {
        ConcurrentHashMap<String, UserCache> users = new ConcurrentHashMap<String, UserCache>();
        userCache.values().forEach(user -> {
            if (user.getData().getGuildId().equals(guildId))
                users.put(user.getData().getUserId(), user);
        });
        return users;
    }

    public static UserCache getUserFromDatabase(@Nonnull String guildId, String userId) {
        // User from a new guild
        if (!DatabaseHandler.collectionExists(Database.USER, guildId)) {
            DatabaseHandler.getDatabase(Database.USER).createCollection(guildId);
            DatabaseHandler.log(LogType.Database, new Document().append("NEW GUILD", guildId));
            return new UserCache(guildId, userId);

        }
        MongoCollection<UserCache> collection = DatabaseHandler.getDatabase(Database.USER).getCollection(guildId, UserCache.class);

        // Get user from Database
        Bson filter = new Document().append("userId", userId);
        FindIterable<UserCache> data = collection.find(filter);
        UserCache first = data.first();
        if (first == null)
            first = new UserCache(guildId, userId);
        return first;
    }
}
