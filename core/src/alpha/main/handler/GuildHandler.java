package alpha.main.handler;

import static alpha.main.AlphaChan.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import alpha.main.BotConfig;
import alpha.main.BotConfig.Config;
import alpha.main.data.user.GuildCache;
import alpha.main.data.user.GuildData;
import alpha.main.handler.DatabaseHandler.Database;
import alpha.main.util.Log;

import net.dv8tion.jda.api.entities.Guild;

public class GuildHandler implements Updatable {

    private static GuildHandler instance = new GuildHandler();
    private static ConcurrentHashMap<String, GuildCache> guildCaches = new ConcurrentHashMap<>();

    private GuildHandler() {
        UpdatableHandler.addListener(this);

        onShutdown.connect((code) -> save());

        Log.system("Guild handler up");
    }

    public synchronized static GuildHandler getInstance() {
        if (instance == null)
            instance = new GuildHandler();
        return instance;
    }

    public static Collection<GuildCache> getGuildCache() {
        return guildCaches.values();
    }

    public void update() {
        updateGuildCache();
    }

    public void updateGuildCache() {
        Iterator<GuildCache> iterator = guildCaches.values().iterator();
        while (iterator.hasNext()) {
            GuildCache guild = iterator.next();
            if (!guild.isAlive(1)) {
                UpdatableHandler.updateStatus();
                guild.update(() -> {
                    guildCaches.remove(guild.getData().getGuildId());
                    Log.info("STATUS",
                            "Guild [" + guild.getGuild().getName() + ":" + guild.getGuild().getId() + "] offline");
                });
            }
        }
    }

    public void save() {

        if (guildCaches.size() == 0)
            return;

        Log.system("Saving guild data");

        Iterator<GuildCache> iterator = guildCaches.values().iterator();
        while (iterator.hasNext()) {
            GuildCache guild = iterator.next();
            guild.update(() -> {
            });
        }
    }

    public static int getActiveGuildCount() {
        return guildCaches.size();
    }

    public synchronized static GuildCache getGuild(Guild guild) {
        if (guild == null)
            throw new IllegalStateException("Guild is not exists");

        return getGuild(guild.getId());
    }

    // Add guild to cache
    public static GuildCache addGuild(String guildId) {
        GuildCache guildData = new GuildCache(guildId);
        guildCaches.put(guildId, guildData);
        return guildData;
    }

    // Get guild from cache/Database
    public synchronized static GuildCache getGuild(String guildId) {
        // If guild exist in cache then return, else query guild from Database
        if (guildCaches.containsKey(guildId)) {
            GuildCache guildData = guildCaches.get(guildId);
            guildData.resetTimer();
            return guildData;
        }

        String guildCollectionName = BotConfig.readString(Config.GUILD_COLLECTION, null);

        // Create new guild cache to store temporary guild data
        if (!DatabaseHandler.collectionExists(Database.GUILD, guildCollectionName)) {
            DatabaseHandler.createCollection(Database.GUILD, guildCollectionName);
            return addGuild(guildId);
        }

        MongoCollection<GuildData> collection = DatabaseHandler.getDatabase(Database.GUILD).getCollection(
                guildCollectionName,
                GuildData.class);

        // Get guild from Database
        Bson filter = new Document().append("guildId", guildId);
        FindIterable<GuildData> data = collection.find(filter).limit(1);
        GuildData first = data.first();

        GuildCache cache;

        if (first != null) {
            cache = new GuildCache(first);
            Log.info("STATUS", "Guild [" + cache.getGuild().getName() + ":" + guildId + "] online");
            guildCaches.put(guildId, cache);
            return cache;

        } else {
            cache = addGuild(guildId);
            Log.info("STATUS", "New guild [" + cache.getGuild().getName() + ":" + guildId + "] online");
            return cache;
        }
    }
}
