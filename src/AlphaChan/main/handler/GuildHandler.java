package AlphaChan.main.handler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.annotation.Nonnull;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import AlphaChan.BotConfig;
import AlphaChan.BotConfig.Config;
import AlphaChan.main.data.user.GuildCache;
import AlphaChan.main.data.user.GuildData;
import AlphaChan.main.handler.DatabaseHandler.Database;
import AlphaChan.main.util.Log;

import net.dv8tion.jda.api.entities.Guild;

import static AlphaChan.AlphaChan.*;

public class GuildHandler implements Updatable {

    private static GuildHandler instance = new GuildHandler();
    private static HashMap<String, GuildCache> guildCaches = new HashMap<>();

    private GuildHandler() {
        UpdatableHandler.addListener(this);

        onShutDown.connect((code) -> save());

        Log.system("Guild handler up");
    }

    public static GuildHandler getInstance() {
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
                Log.info("STATUS", "Guild <" + guild.getGuild().getName() + ":" + guild.getGuild().getId() + "> offline");
                UpdatableHandler.updateStatus();
                guild.update();
                iterator.remove();
            }
        }
    }

    public void save() {
        Iterator<GuildCache> iterator = guildCaches.values().iterator();
        while (iterator.hasNext()) {
            GuildCache guild = iterator.next();
            guild.update();
        }
    }

    public static int getActiveGuildCount() {
        return guildCaches.size();
    }

    public static GuildCache getGuild(Guild guild) {
        if (guild == null)
            return null;
        return getGuild(guild.getId());
    }

    // Add guild to cache
    public static GuildCache addGuild(@Nonnull String guildId) {
        GuildCache guildData = new GuildCache(guildId);
        guildCaches.put(guildId, guildData);
        return guildData;
    }

    // Get guild from cache/Database
    public static GuildCache getGuild(@Nonnull String guildId) {
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

        MongoCollection<GuildData> collection = DatabaseHandler.getDatabase(Database.GUILD).getCollection(guildCollectionName,
                GuildData.class);

        // Get guild from Database
        Bson filter = new Document().append("guildId", guildId);
        FindIterable<GuildData> data = collection.find(filter).limit(1);
        GuildData first = data.first();

        GuildCache cache;

        if (first != null) {
            cache = new GuildCache(first);
            Log.info("STATUS", "Guild <" + cache.getGuild().getName() + ":" + guildId + "> online");
            guildCaches.put(guildId, cache);
            return cache;

        } else {
            cache = addGuild(guildId);
            Log.info("STATUS", "New guild <" + cache.getGuild().getName() + ":" + guildId + "> online");
            return cache;
        }
    }
}
