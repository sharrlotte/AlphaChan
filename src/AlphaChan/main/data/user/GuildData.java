package AlphaChan.main.data.user;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;

import AlphaChan.BotConfig;
import AlphaChan.BotConfig.Config;
import AlphaChan.main.handler.DatabaseHandler;
import AlphaChan.main.handler.UserHandler;
import AlphaChan.main.handler.DatabaseHandler.DATABASE;
import AlphaChan.main.handler.DatabaseHandler.LOG_TYPE;
import AlphaChan.main.util.Log;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import static AlphaChan.AlphaChan.*;

public class GuildData extends TimeObject {

    public enum CHANNEL_TYPE {
        SCHEMATIC, MAP, SERVER_STATUS, BOT_LOG
    }

    public enum BOOL {
        TRUE, FALSE, UNDEFINED
    }

    @Nonnull
    public String guildId = new String();

    public String showLevel = BOOL.UNDEFINED.name();

    public List<String> adminRoleId = new ArrayList<String>();
    // Schematic channel id, map channel id
    public ConcurrentHashMap<String, List<String>> channelId = new ConcurrentHashMap<String, List<String>>();
    // Roles that require level to achieve
    public ConcurrentHashMap<String, Integer> levelRoleId = new ConcurrentHashMap<String, Integer>();

    private Guild guild;
    private boolean deleted = false;

    // For codec
    public GuildData() {
        super(BotConfig.readInt(Config.GUILD_ALIVE_TIME, 10), BotConfig.readInt(Config.UPDATE_LIMIT, 10));
    }

    public GuildData(@Nonnull String guildId) {
        super(BotConfig.readInt(Config.GUILD_ALIVE_TIME, 10), BotConfig.readInt(Config.UPDATE_LIMIT, 10));
        this.guildId = guildId;
        _getGuild();
    }

    public void setShowLevel(String showLevel) {
        this.showLevel = showLevel;
    }

    public String getShowLevel() {
        return this.showLevel;
    }

    public void setAdminRoleId(List<String> adminRoleId) {
        this.adminRoleId = adminRoleId;
    }

    public List<String> getAdminRoleId() {
        return this.adminRoleId;
    }

    public void setChannelId(ConcurrentHashMap<String, List<String>> channelId) {
        this.channelId = channelId;
    }

    public ConcurrentHashMap<String, List<String>> getChannelId() {
        return this.channelId;
    }

    public void setLevelRoleId(ConcurrentHashMap<String, Integer> levelRoleId) {
        this.levelRoleId = levelRoleId;
    }

    public ConcurrentHashMap<String, Integer> getLevelRoleId() {
        return this.levelRoleId;
    }

    public Document toDocument() {
        return new Document().append("guildId", this.guildId).//
                append("showLevel", this.showLevel).//
                append("adminRoleId", this.adminRoleId).//
                append("channelId", this.channelId).//
                append("levelRoleId", this.levelRoleId);
    }

    public @Nonnull Guild _getGuild() {
        Guild guild = jda.getGuildById(this.guildId);
        if (guild == null) {
            deleted = true;
            throw new IllegalStateException("Guild not found with id <" + guildId + ">");
        }
        this.guild = guild;
        return guild;
    }

    public boolean _displayLevel(String showLevel) {

        Member bot = _getGuild().getMember(jda.getSelfUser());
        if (bot == null)
            throw new IllegalStateException("Bot not in guild " + guildId);

        // Loop through guild members and modify their nickname
        guild.getMembers().forEach(member -> {
            if (member != null)
                if (bot.canInteract(member)) {

                    Log.system("Changing name " + member.getEffectiveName());
                    UserData data = UserHandler.getUserNoCache(member);
                    if (data != null) {
                        data.setName(member.getUser().getName());
                        data._displayLevelName();
                    }
                }
        });
        this.showLevel = showLevel;
        update();
        return true;
    }

    public boolean _containsChannel(String channel_type, String channelId) {
        List<String> channelIds = this.channelId.get(channel_type);
        if (channelIds == null)
            return false;
        return channelIds.contains(channelId);
    }

    public List<TextChannel> _getChannels(String channel_type) {
        List<String> channelIds = this.channelId.get(channel_type);
        if (channelIds == null)
            return null;

        TextChannel temp;
        List<TextChannel> channels = new ArrayList<TextChannel>();
        for (String c : channelIds) {
            if (c == null)
                continue;
            temp = _getGuild().getTextChannelById(c);
            if (temp == null)
                continue;
            channels.add(temp);
        }
        return channels;
    }

    public boolean _addChannel(String channel_type, String channel_id) {
        if (channelId.get(channel_type) == null)
            channelId.put(channel_type, new ArrayList<String>());

        if (channelId.get(channel_type).contains(channel_id))
            return false;
        boolean result = channelId.get(channel_type).add(channel_id);
        update();
        return result;

    }

    public boolean _removeChannel(String channel_type, String channel_id) {
        if (channelId.get(channel_type) == null)
            return false;
        boolean result = channelId.get(channel_type).remove(channel_id);
        update();
        return result;
    }

    public boolean _addRole(String roleId, int level) {
        boolean result = levelRoleId.put(roleId, level) == null;
        update();
        return result;
    }

    public boolean _removeRole(String roleId) {
        boolean result = levelRoleId.remove(roleId) != null;
        update();
        return result;
    }

    // Update guild on database
    @Override
    public void update() {
        // If this guild is deleted. don't save
        if (deleted)
            return;
        // Create collection if it's not exist
        String guildCollectionName = BotConfig.readString(Config.GUILD_COLLECTION, null);
        MongoCollection<GuildData> collection = DatabaseHandler.getCollection(DATABASE.GUILD, guildCollectionName,
                GuildData.class);

        if (this.showLevel == BOOL.UNDEFINED.name())
            this.showLevel = BOOL.FALSE.name();

        // Filter for guild id, guild id is unique for each collection
        Bson filter = new Document().append("guildId", this.guildId);
        collection.replaceOne(filter, this, new ReplaceOptions().upsert(true));
    }

    public void delete() {
        String guildCollectionName = BotConfig.readString(Config.GUILD_COLLECTION, null);

        MongoCollection<GuildData> collection = DatabaseHandler.getCollection(DATABASE.GUILD, guildCollectionName,
                GuildData.class);
        if (this.showLevel == BOOL.UNDEFINED.name())
            this.showLevel = BOOL.FALSE.name();

        // Filter for guild id, guild id is unique for each collection
        Bson filter = new Document().append("guildId", this.guildId);
        collection.deleteOne(filter);
        DatabaseHandler.log(LOG_TYPE.DATABASE, new Document().append("DELETE GUILD", this.toDocument()));
        this.deleted = true;
    }
}
