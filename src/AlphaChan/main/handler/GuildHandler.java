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
import AlphaChan.main.data.user.GuildData;
import AlphaChan.main.handler.DatabaseHandler.DATABASE;
import AlphaChan.main.util.Log;

import net.dv8tion.jda.api.entities.Guild;

public class GuildHandler implements Updatable {

    private static GuildHandler instance = new GuildHandler();
    private static HashMap<String, GuildData> guildCache = new HashMap<>();

    private GuildHandler() {
        UpdatableHandler.addListener(this);

        Log.system("Guild handler up");
    }

    public static GuildHandler getInstance() {
        if (instance == null)
            instance = new GuildHandler();
        return instance;
    }

    public static Collection<GuildData> getGuildCache() {
        return guildCache.values();
    }

    public void update() {
        updateGuildCache();
    }

    public void updateGuildCache() {
        Iterator<GuildData> iterator = guildCache.values().iterator();
        while (iterator.hasNext()) {
            GuildData guild = iterator.next();
            if (!guild.isAlive(1)) {
                Log.info("STATUS", "Guild <" + guild._getGuild().getName() + ":" + guild.guildId + "> offline");
                UpdatableHandler.updateStatus();
                guild.update();
                iterator.remove();

            }
        }
    }

    public static int getActiveGuildCount() {
        return guildCache.size();
    }

    public static GuildData getGuild(Guild guild) {
        if (guild == null)
            return null;
        return getGuild(guild.getId());
    }

    // Add guild to cache
    public static GuildData addGuild(@Nonnull String guildId) {
        GuildData guildData = new GuildData(guildId);
        guildCache.put(guildId, guildData);
        return guildData;
    }

    // Get guild from cache/database
    public static GuildData getGuild(@Nonnull String guildId) {
        // If guild exist in cache then return, else query guild from database
        if (guildCache.containsKey(guildId)) {
            GuildData guildData = guildCache.get(guildId);
            guildData.resetTimer();
            return guildData;
        }

        String guildCollectionName = BotConfig.readString(Config.GUILD_COLLECTION, "GUILD_COLLECTION");

        // Create new guild cache to store temporary guild data
        if (!DatabaseHandler.collectionExists(DATABASE.GUILD, guildCollectionName)) {
            DatabaseHandler.createCollection(DATABASE.GUILD, guildCollectionName);
            return addGuild(guildId);
        }

        MongoCollection<GuildData> collection = DatabaseHandler.getDatabase(DATABASE.GUILD)
                .getCollection(guildCollectionName, GuildData.class);

        // Get guild from database
        Bson filter = new Document().append("guildId", guildId);
        FindIterable<GuildData> data = collection.find(filter).limit(1);
        GuildData first = data.first();
        UpdatableHandler.updateStatus();
        if (first != null) {
            Log.info("STATUS", "Guild <" + first._getGuild().getName() + ":" + guildId + "> online");
            guildCache.put(guildId, first);
            return first;
        } else {
            first = addGuild(guildId);
            Log.info("STATUS", "New guild <" + first._getGuild().getName() + ":" + guildId + "> online");
            return first;
        }
    }
}
