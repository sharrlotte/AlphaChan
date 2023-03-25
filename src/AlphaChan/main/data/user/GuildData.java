package AlphaChan.main.data.user;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.Document;

public class GuildData {

    private String guildId = new String();

    private List<String> adminRoleId = new ArrayList<>();
    // Schematic channel id, map channel id
    private ConcurrentHashMap<String, List<String>> channelId = new ConcurrentHashMap<>();
    // Roles that require level to achieve
    private ConcurrentHashMap<String, Integer> levelRoleId = new ConcurrentHashMap<>();

    // For codec
    public GuildData() {
    }

    public String getGuildId() {
        return this.guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
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
                append("adminRoleId", this.adminRoleId).//
                append("channelId", this.channelId).//
                append("levelRoleId", this.levelRoleId);
    }
}
