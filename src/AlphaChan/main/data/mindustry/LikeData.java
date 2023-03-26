package AlphaChan.main.data.mindustry;

import org.bson.BsonDateTime;

public class LikeData {

    private String userId;
    private BsonDateTime time;

    public LikeData() {

    }

    public LikeData(String userId, long time) {
        this(userId, new BsonDateTime(time));
    }

    public LikeData(String userId, BsonDateTime time) {
        this.userId = userId;
        this.time = time;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return this.userId;
    }

    public BsonDateTime getTime() {
        return time;
    }

    public void setTime(BsonDateTime time) {
        this.time = time;
    }
}
