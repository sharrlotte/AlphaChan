package AlphaChan.main.data.mindustry;

import org.bson.Document;

import AlphaChan.BotConfig;
import AlphaChan.BotConfig.Config;
import AlphaChan.main.data.user.DatabaseObject;
import AlphaChan.main.data.user.TimeObject;
import AlphaChan.main.handler.DatabaseHandler;
import AlphaChan.main.handler.DatabaseHandler.Database;

public class SchematicInfoCache extends TimeObject implements DatabaseObject {

    private SchematicInfo data;

    public SchematicInfoCache(SchematicInfo data) {
        this.data = data;
    }

    public SchematicInfo getData() {
        return data;
    }

    public long getLike() {
        if (data.getLike() != -1)
            return data.getLike();

        long like = DatabaseHandler.count(Database.LIKE, data.getId(), DislikeData.class, null);
        getData().setLike(like);

        // Create collection if it's not exist
        return DatabaseHandler.count(Database.LIKE, data.getId(), DislikeData.class, null);
    }

    public boolean addLike(String userId) {

        Document filter = new Document().append("userId", userId);
        boolean result = DatabaseHandler.insertIfNotFound(Database.LIKE, data.getId(), DislikeData.class, filter,
                new DislikeData(userId, System.currentTimeMillis()));

        if (result == true)
            data.setLike(data.getLike() + 1);

        return result;
    }

    public long getDislike() {
        if (data.getDislike() != -1)
            return data.getDislike();
        // Create collection if it's not exist

        long dislike = DatabaseHandler.count(Database.DISLIKE, data.getId(), LikeData.class, null);
        getData().setDislike(dislike);

        return dislike;
    }

    public boolean addDislike(String userId) {

        Document filter = new Document().append("userId", userId);
        boolean result = DatabaseHandler.insertIfNotFound(Database.DISLIKE, data.getId(), LikeData.class, filter,
                new LikeData(userId, System.currentTimeMillis()));

        if (result == true)
            data.setLike(data.getLike() + 1);

        return result;
    }

    @Override
    public void update(Runnable cacheCleaner) {
        if (isAlive()) {
            String schematicInfoCollectionName = BotConfig.readString(Config.SCHEMATIC_INFO_COLLECTION, null);
            Document filter = new Document().append("_id", data.getId());
            DatabaseHandler.update(Database.MINDUSTRY, schematicInfoCollectionName, SchematicInfo.class, filter, data);
        }
    }

    @Override
    public void delete() {
        if (isAlive()) {
            String schematicInfoCollectionName = BotConfig.readString(Config.SCHEMATIC_INFO_COLLECTION, null);
            Document filter = new Document().append("_id", data.getId());
            DatabaseHandler.delete(Database.MINDUSTRY, schematicInfoCollectionName, SchematicInfo.class, filter);
            kill();
        }
    }

}
