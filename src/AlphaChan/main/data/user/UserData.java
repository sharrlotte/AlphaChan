package AlphaChan.main.data.user;

import java.util.Arrays;

import org.bson.Document;

public class UserData extends TimeObject {

    private String userId = new String();
    private String name = new String();
    private String guildId = new String();

    private int[] pointList = new int[UserCache.PointType.values().length];

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGuildId() {
        return this.guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public int[] getPoints() {
        return this.pointList;
    }

    public void setPoints(int[] point) {
        this.pointList = point;
    }

    // For codec
    public UserData() {

    }

    public UserData(String guildId, String userId, String name, Integer point, Integer level, Integer money,
            Integer pvpPoint) {
        this.userId = userId;
        this.guildId = guildId;
        this.name = name;
    }

    @Override
    public String toString() {
        return "userId:" + this.userId + "\n" + "guildId:" + this.guildId + "\n" + "point:" + Arrays.toString(pointList)
                + "\n";
    }

    public Document toDocument() {
        return new Document().append("userId", this.userId).//
                append("name", this.name).//
                append("guildId", this.guildId).//
                append("point", this.pointList);
    }

}
