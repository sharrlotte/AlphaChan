package AlphaChan.main.data.user;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;

import AlphaChan.BotConfig;
import AlphaChan.BotConfig.Config;
import AlphaChan.main.handler.DatabaseHandler;
import AlphaChan.main.handler.DatabaseHandler.Database;
import AlphaChan.main.handler.DatabaseHandler.LogType;

import static AlphaChan.AlphaChan.jda;

public class GuildCache extends TimeObject implements DatabaseObject {

    public static enum ChannelType {
        SCHEMATIC, MAP, SERVER_STATUS, BOT_LOG
    }

    private final GuildData data;

    public GuildCache(String guildId) {
        this.data = new GuildData();
        this.data.setGuildId(guildId);
    }

    public GuildCache(GuildData data) {
        this.data = data;
    }

    public GuildData getData() {
        return data;
    }

    public Guild getGuild() {
        Guild guild = jda.getGuildById(data.getGuildId());

        if (guild == null) {
            killTimer();
            throw new IllegalStateException("Guild not found with id <" + data.getGuildId() + ">");
        }

        return guild;
    }

    public boolean hasChannel(ChannelType channelType, String channelId) {
        List<String> channelIds = data.getChannelId().get(channelType.name());
        if (channelIds == null)
            return false;
        return channelIds.contains(channelId);
    }

    public List<TextChannel> getChannels(ChannelType channelType) {
        List<String> channelIds = data.getChannelId().get(channelType.name());
        if (channelIds == null)
            return null;

        TextChannel temp;
        List<TextChannel> channels = new ArrayList<TextChannel>();

        for (String channelId : channelIds) {
            temp = getGuild().getTextChannelById(channelId);
            if (temp != null)
                channels.add(temp);
        }
        return channels;
    }

    public boolean addChannel(ChannelType channelType, String channelId) {
        if (data.getChannelId().get(channelType.name()) == null)
            data.getChannelId().put(channelType.name(), new ArrayList<String>());

        List<String> channelIds = data.getChannelId().get(channelType.name());

        if (channelIds.contains(channelId))
            return false;

        return channelIds.add(channelId);

    }

    public boolean removeChannel(ChannelType channelType, String channelId) {
        if (data.getChannelId().get(channelType.name()) == null)
            return false;

        return data.getChannelId().get(channelType.name()).remove(channelId);
    }

    public boolean addLevelRole(String roleId, int level) {
        return data.getLevelRoleId().put(roleId, level) == null;
    }

    public boolean removeLevelRole(String roleId) {
        return data.getLevelRoleId().remove(roleId) != null;
    }

    // Update guild on Database
    @Override
    public void update() {
        // If this guild is deleted. don't save
        if (isAlive())
            return;
        // Create collection if it's not exist
        String guildCollectionName = BotConfig.readString(Config.GUILD_COLLECTION, null);
        MongoCollection<GuildData> collection = DatabaseHandler.getCollection(Database.GUILD, guildCollectionName,
                GuildData.class);

        // Filter for guild id, guild id is unique for each collection
        Bson filter = new Document().append("guildId", data.getGuildId());
        collection.replaceOne(filter, data, new ReplaceOptions().upsert(true));
    }

    @Override
    public void delete() {
        String guildCollectionName = BotConfig.readString(Config.GUILD_COLLECTION, null);

        MongoCollection<GuildData> collection = DatabaseHandler.getCollection(Database.GUILD, guildCollectionName,
                GuildData.class);

        // Filter for guild id, guild id is unique for each collection
        Bson filter = new Document().append("guildId", data.getGuildId());
        collection.deleteOne(filter);

        DatabaseHandler.log(LogType.Database, new Document().append("DELETE GUILD", data.toDocument()));
        killTimer();
    }
}
