package AlphaChan.main.data.mindustry;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;

import AlphaChan.BotConfig;
import AlphaChan.BotConfig.Config;
import AlphaChan.main.handler.DatabaseHandler;
import AlphaChan.main.handler.DatabaseHandler.DATABASE;

public class SchematicData {

    public String id;
    public String data;
    private boolean deleted = false;

    public SchematicData() {
    }

    public SchematicData(String id, String data) {
        this.id = id;
        this.data = data;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getData() {
        return this.data;
    }

    public void update() {
        if (deleted)
            return;

        String schematicDataCollectionName = BotConfig.readString(Config.SCHEMATIC_DATA_COLLECTION, null);

        // Create collection if it's not exist
        if (!DatabaseHandler.collectionExists(DATABASE.MINDUSTRY, schematicDataCollectionName)) {
            DatabaseHandler.createCollection(DATABASE.MINDUSTRY, schematicDataCollectionName);
        }
        MongoCollection<SchematicData> collection = DatabaseHandler.getDatabase(DATABASE.MINDUSTRY)
                .getCollection(schematicDataCollectionName, SchematicData.class);

        Bson filter = new Document().append("_id", this.id);
        collection.replaceOne(filter, this, new ReplaceOptions().upsert(true));
    }

    public void delete() {
        String schematicDataCollectionName = BotConfig.readString(Config.SCHEMATIC_DATA_COLLECTION, null);

        // Create collection if it's not exist
        if (!DatabaseHandler.collectionExists(DATABASE.MINDUSTRY, schematicDataCollectionName)) {
            DatabaseHandler.createCollection(DATABASE.MINDUSTRY, schematicDataCollectionName);
        }
        MongoCollection<SchematicInfo> collection = DatabaseHandler.getDatabase(DATABASE.MINDUSTRY)
                .getCollection(schematicDataCollectionName, SchematicInfo.class);

        Document filter = new Document().append("_id", this.id);
        collection.deleteOne(filter);
        this.deleted = true;
    }
}
