package AlphaChan.main.handler;

import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import AlphaChan.BotConfig;
import AlphaChan.BotConfig.Config;
import AlphaChan.main.data.user.GuildCache;
import AlphaChan.main.data.user.UserCache;
import AlphaChan.main.data.user.UserData;
import AlphaChan.main.data.user.UserCache.PointType;
import AlphaChan.main.handler.DatabaseHandler.Database;
import AlphaChan.main.util.Log;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;

import static AlphaChan.AlphaChan.*;

public final class UserHandler implements Updatable {

    private static UserHandler instance = new UserHandler();
    // Hash map to store user cache
    private static ConcurrentHashMap<String, UserCache> userCache = new ConcurrentHashMap<>();

    private UserHandler() {
        UpdatableHandler.addListener(this);

        onShutdown.connect((code) -> save());

        Log.system("User handler up");
    }

    public synchronized static UserHandler getInstance() {
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
            if (!user.isAlive(1))
                try {
                    user.update(() -> userCache.remove(user.getHashId()));
                    Log.info("STATUS", "User [" + user.getName() + ":" + user.getData().getUserId() + "] offline");

                } catch (Exception e) {
                    Log.error(e);
                }
        }
        UpdatableHandler.updateStatus();
    }

    public void save() {
        if (userCache.size() == 0)
            return;

        Log.system("Saving user data");

        Iterator<UserCache> iterator = userCache.values().iterator();
        while (iterator.hasNext()) {
            UserCache user = iterator.next();
            try {
                user.update(() -> {
                });
            } catch (Exception exception) {
                Log.error(exception);
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

        if (user.addExp(1))
            user.checkLevelRole();
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
    public synchronized static UserCache addUser(String guildId, String userId) {
        UserCache userData = new UserCache(guildId, userId);
        // Key is hashId = guildId + userId
        userCache.put(userData.getHashId(), userData);
        Log.info("STATUS", "User [" + userData.getName() + ":" + userId + "] online");
        UpdatableHandler.updateStatus();
        return userData;
    }

    // Add user to cache
    public static UserCache addUser(Member member) {
        return addUser(member.getGuild().getId(), member.getId());
    }

    public synchronized static UserCache getUserNoCache(String guildId, String userId) {
        String hashId = guildId + userId;
        if (userCache.containsKey(hashId))
            return userCache.get(hashId);

        // If user exist in cache then return, else query user from Database
        return getUserFromDatabase(guildId, userId);
    }

    // Get user without adding it to cache
    public synchronized static UserCache getUserNoCache(Member member) {
        String guildId = member.getGuild().getId();
        String userId = member.getId();
        return getUserNoCache(guildId, userId);
    }

    // Waiting for data from Database
    public synchronized static UserCache getUserAwait(Member member) {
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

    public static ConcurrentHashMap<String, UserCache> getUserFromGuild(String guildId) {
        ConcurrentHashMap<String, UserCache> users = new ConcurrentHashMap<String, UserCache>();
        userCache.values().forEach(user -> {
            if (user.getData().getGuildId().equals(guildId))
                users.put(user.getData().getUserId(), user);
        });
        return users;
    }

    public static UserCache getUserFromDatabase(String guildId, String userId) {

        MongoCollection<UserData> collection = DatabaseHandler.getCollection(Database.USER, guildId, UserData.class);

        // Get user from Database
        Bson filter = new Document().append("userId", userId);
        FindIterable<UserData> data = collection.find(filter);
        UserData first = data.first();
        UserCache cache;

        if (first == null)
            cache = new UserCache(guildId, userId);
        else
            cache = new UserCache(first);

        return cache;
    }
}
