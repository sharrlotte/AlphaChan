package alpha.main.data.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;

public class UserData {

    private String userId = new String();
    private String name = new String();
    private String guildId = new String();

    private List<Integer> points = Arrays.asList(0, 0, 0, 0);// new ArrayList<>(UserCache.PointType.values().length);

    // For codec
    public UserData() {

    }

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

    public List<Integer> getPoints() {
        return this.points;
    }

    public void setPoints(ArrayList<Integer> points) {
        this.points = points;
    }

    public Document toDocument() {
        return new Document().append("userId", this.userId).//
                append("name", this.name).//
                append("guildId", this.guildId).//
                append("point", this.points);
    }
}
