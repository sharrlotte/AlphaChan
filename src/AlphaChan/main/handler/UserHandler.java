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
import AlphaChan.main.handler.DatabaseHandler.DATABASE;
import AlphaChan.main.handler.DatabaseHandler.LOG_TYPE;
import AlphaChan.main.user.GuildData;
import AlphaChan.main.user.UserData;
import AlphaChan.main.util.Log;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;

public final class UserHandler {

    private static UserHandler instance = new UserHandler();
    // Hash map to store user cache
    private static ConcurrentHashMap<String, UserData> userCache = new ConcurrentHashMap<>();

    private UserHandler() {
        Log.info("SYSTEM", "User handler up");
    }

    public static UserHandler getInstance() {
        if (instance == null)
            instance = new UserHandler();
        return instance;
    }

    public static Collection<UserData> getUserCache() {
        return userCache.values();
    }

    public static void update() {
        updateCache();
    }

    public static void updateCache() {
        Iterator<UserData> iterator = userCache.values().iterator();
        while (iterator.hasNext()) {
            UserData user = iterator.next();
            if (!user.isAlive(1)) {
                Log.info("STATUS", "User <" + user.getName() + ":" + user.userId + "> offline");
                UpdatableHandler.updateStatus();
                user.update();
                iterator.remove();
            }
        }
    }

    public static int getActiveUserCount() {
        return userCache.size();
    }

    public static Collection<UserData> getCachedUser() {
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
        UserData user = getUserAwait(member);
        user.resetTimer();

        user._addMoney(1);
        user._addPoint(1);
        user._checkLevelRole();
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

        GuildData guildData = GuildHandler.getGuild(member.getGuild().getId());
        for (String adminId : guildData.adminRoleId) {
            for (Role role : roles) {
                if (role.getId().equals(adminId))
                    return true;
            }
        }
        return false;
    }

    // Add user to cache
    public static UserData addUser(@Nonnull String guildId, @Nonnull String userId) {
        UserData userData = new UserData(guildId, userId);
        // Key is hashId = guildId + userId
        userCache.put(userData._getHashId(), userData);
        Log.info("STATUS", "User <" + userData.getName() + ":" + userId + "> online");
        UpdatableHandler.updateStatus();
        return userData;
    }

    // Add user to cache
    public static UserData addUser(Member member) {
        return addUser(member.getGuild().getId(), member.getId());
    }

    public static UserData getUserNoCache(@Nonnull String guildId, @Nonnull String userId) {
        String hashId = guildId + userId;
        if (userCache.containsKey(hashId))
            return userCache.get(hashId);
        return getUserFromDatabase(guildId, userId);
    }

    // Get user without adding it to cache
    public static UserData getUserNoCache(@Nonnull Member member) {
        String guildId = member.getGuild().getId();
        String userId = member.getId();
        // If user exist in cache then return, else query user from database
        return getUserNoCache(guildId, userId);
    }

    // Waiting for data from database
    public static UserData getUserAwait(@Nonnull Member member) {
        String guildId = member.getGuild().getId();
        String userId = member.getId();
        // If user exist in cache then return, else query user from database
        String hashId = guildId + userId;
        if (userCache.containsKey(hashId))
            return userCache.get(hashId);

        UserData userFromCache = addUser(guildId, userId);
        UserData userFromDatabase = getUserFromDatabase(guildId, userId);
        userFromDatabase._merge(userFromCache);
        userCache.put(hashId, userFromDatabase);
        return userFromDatabase;
    }

    public static ConcurrentHashMap<String, UserData> getUserFromGuild(@Nonnull String guildId) {
        ConcurrentHashMap<String, UserData> users = new ConcurrentHashMap<String, UserData>();
        userCache.values().forEach(user -> {
            if (user.guildId.equals(guildId))
                users.put(user.userId, user);
        });
        return users;
    }

    public static UserData getUserFromDatabase(@Nonnull String guildId, @Nonnull String userId) {
        // User from a new guild
        if (!DatabaseHandler.collectionExists(DATABASE.USER, guildId)) {
            DatabaseHandler.getDatabase(DATABASE.USER).createCollection(guildId);
            DatabaseHandler.log(LOG_TYPE.DATABASE, new Document().append("NEW GUILD", guildId));
            return new UserData(guildId, userId);

        }
        MongoCollection<UserData> collection = DatabaseHandler.getDatabase(DATABASE.USER).getCollection(guildId,
                UserData.class);

        // Get user from database
        Bson filter = new Document().append("userId", userId);
        FindIterable<UserData> data = collection.find(filter);
        UserData first = data.first();
        if (first == null)
            first = new UserData(guildId, userId);
        return first;
    }
}
